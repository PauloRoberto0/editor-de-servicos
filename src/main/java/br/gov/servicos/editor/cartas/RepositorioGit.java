package br.gov.servicos.editor.cartas;

import br.gov.servicos.editor.servicos.Revisao;
import br.gov.servicos.editor.utils.LogstashProgressMonitor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.marker.LogstashMarker;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.internal.JGitText;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;
import org.slf4j.Marker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import static br.gov.servicos.editor.utils.Unchecked.Consumer.uncheckedConsumer;
import static br.gov.servicos.editor.utils.Unchecked.Function.uncheckedFunction;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;
import static lombok.AccessLevel.PRIVATE;
import static net.logstash.logback.marker.Markers.append;
import static org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode.TRACK;
import static org.eclipse.jgit.lib.Constants.*;
import static org.eclipse.jgit.merge.MergeStrategy.THEIRS;

@Slf4j
@Service
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class RepositorioGit {

    File raiz;
    boolean fazerPush;

    @NonFinal
    Git git; // disponível enquanto o repositório estiver aberto

    @Autowired
    public RepositorioGit(File repositorioCartasLocal, @Value("${flags.git.push}") boolean fazerPush) {
        this.raiz = repositorioCartasLocal;
        this.fazerPush = fazerPush;
    }

    public Path getCaminhoAbsoluto() {
        return raiz.getAbsoluteFile().toPath();
    }

    public Optional<Revisao> getRevisaoMaisRecenteDoArquivo(Path caminhoRelativo) {
        RevCommit commit = comRepositorioAbertoParaLeitura(uncheckedFunction(git -> {

            Repository repo = git.getRepository();
            RevWalk revWalk = new RevWalk(repo);

            revWalk.setTreeFilter(AndTreeFilter.create(PathFilter.create(caminhoRelativo.toString()), TreeFilter.ANY_DIFF));
            revWalk.markStart(revWalk.lookupCommit(repo.resolve(HEAD)));

            Iterator<RevCommit> revs = revWalk.iterator();
            if (revs.hasNext()) {
                return revs.next();
            }
            return null;
        }));

        if (commit == null) {
            return empty();
        }

        return of(new Revisao(commit));
    }

    public Optional<Revisao> getRevisaoMaisRecenteDoBranch(String branch) {
        RevCommit commit = comRepositorioAbertoParaLeitura(uncheckedFunction(git -> {
            Repository repo = git.getRepository();
            Ref ref = repo.getRef(branch);

            if (ref == null) {
                return null;
            }

            return new RevWalk(repo).parseCommit(ref.getObjectId());
        }));

        if (commit == null) {
            return empty();
        }

        return of(new Revisao(commit));
    }

    @SneakyThrows
    private <T> T comRepositorioAberto(Function<Git, T> fn) {
        try (Git git = Git.open(raiz)) {
            synchronized (Git.class) {
                try {
                    this.git = git;
                    return fn.apply(git);
                } finally {
                    this.git = null;
                }
            }
        }
    }

    @SneakyThrows
    private <T> T comRepositorioAbertoParaLeitura(Function<Git, T> fn) {
        try (Git git = Git.open(raiz)) {
            try {
                this.git = git;
                return fn.apply(git);
            } finally {
                this.git = null;
            }
        }
    }

    @SneakyThrows
    public <T> T comRepositorioAbertoNoBranch(String branch, Supplier<T> supplier) {
        return comRepositorioAberto(git -> {
            checkout(branch);
            try {
                return supplier.get();
            } finally {
                checkoutMaster();
            }
        });
    }

    @SneakyThrows
    private void checkoutMaster() {
        Ref result = git.checkout()
                .setName(R_HEADS + MASTER)
                .call();

        Marker marker = append("git.branch", git.getRepository().getBranch())
                .and(append("git.state", git.getRepository().getRepositoryState().toString()))
                .and(append("checkout.to", MASTER))
                .and(append("checkout.result", result.getName()));

        log.info(marker, "git checkout master");
    }

    @SneakyThrows
    private void checkout(String branch) {
        String novoBranch = branch.replaceAll("^" + R_HEADS, "");

        LogstashMarker info = append("git.branch", git.getRepository().getBranch())
                .and(append("git.state", git.getRepository().getRepositoryState().toString()))
                .and(append("checkout.to", novoBranch));

        if (git.getRepository().getRefDatabase().getRef(branch) == null) {
            Ref result = git.checkout()
                    .setName(novoBranch)
                    .setStartPoint(R_HEADS + MASTER)
                    .setUpstreamMode(TRACK)
                    .setCreateBranch(true)
                    .call();


            Marker marker = info.and(append("checkout.result", result.getName()))
                    .and(append("checkout.branch.created", false));

            log.info(marker, "git checkout {}", novoBranch);

        } else {
            Ref result = git.checkout()
                    .setName(novoBranch)
                    .setCreateBranch(false)
                    .call();

            Marker marker = info.and(append("checkout.result", result.getName()))
                    .and(append("checkout.branch.created", true));

            log.info(marker, "git checkout {}", novoBranch);
        }
    }

    @SneakyThrows
    public void add(Path caminho) {
        Marker marker = append("git.state", git.getRepository().getRepositoryState().toString())
                .and(append("git.branch", git.getRepository().getBranch()));

        log.debug(marker, "git add: {}", git.getRepository().getBranch(), caminho);

        git.add()
                .addFilepattern(caminho.toString())
                .call();
    }

    @SneakyThrows
    public void commit(Path caminho, String mensagem, User usuario) {
        PersonIdent ident = new PersonIdent(usuario.getUsername(), "servicos@planejamento.gov.br");
        try {
            RevCommit result = git.commit()
                    .setMessage(mensagem)
                    .setCommitter(ident)
                    .setAuthor(ident)
                    .setOnly(caminho.toString())
                    .call();

            Marker marker = append("commit", result.getName())
                    .and(append("commit.message", mensagem))
                    .and(append("commit.author", ident.getName()))
                    .and(append("commit.email", ident.getEmailAddress()))
                    .and(append("commit.path", caminho.toString()))
                    .and(append("git.branch", git.getRepository().getBranch()))
                    .and(append("git.state", git.getRepository().getRepositoryState().toString()));

            log.info(marker, "git commit {}", caminho);

        } catch (JGitInternalException e) {
            if (e.getMessage().equals(JGitText.get().emptyCommit)) {
                log.info("Commit não possui alterações em {}", caminho);
            } else {
                throw e;
            }
        }
    }

    public void pull() {
        try {
            PullResult result = git.pull()
                    .setRebase(true)
                    .setStrategy(THEIRS)
                    .setProgressMonitor(new LogstashProgressMonitor(log))
                    .call();

            Marker marker = append("git.state", git.getRepository().getRepositoryState().toString())
                    .and(append("git.branch", git.getRepository().getBranch()))
                    .and(append("pull.fetched.from", result.getFetchedFrom()))
                    .and(append("pull.fetch.result.updates", result.getFetchResult() == null ? null : result.getFetchResult().getMessages()))
                    .and(append("pull.fetch.result.updates", result.getFetchResult() == null ? null : result.getFetchResult().getTrackingRefUpdates().stream().map(TrackingRefUpdate::getResult).map(Enum::toString).collect(toList())))
                    .and(append("pull.rebase.result", result.getRebaseResult() == null ? null : result.getRebaseResult().getStatus().toString()))
                    .and(append("pull.merge.result", result.getMergeResult() == null ? null : result.getMergeResult().getMergeStatus().toString()));

            log.info(marker, "git pull em {}", git.getRepository().getBranch());

            if (!result.isSuccessful()) {
                throw new IllegalStateException("Não foi possível completar o git pull");
            }

        } catch (IOException | GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    public void push(String branch) {
        if (!fazerPush) {
            log.info("Envio de alterações ao Github desligado (FLAGS_GIT_PUSH=false)");
            return;
        }

        try {
            git.push()
                    .setRemote(DEFAULT_REMOTE_NAME)
                    .setRefSpecs(new RefSpec(branch + ":" + branch))
                    .setProgressMonitor(new LogstashProgressMonitor(log))
                    .call()
                    .forEach(uncheckedConsumer(result -> {
                        Marker marker = append("push.messages", result.getMessages())
                                .and(append("push.updates", result.getRemoteUpdates().stream().map(u -> u.getStatus().toString()).collect(toList())))
                                .and(append("git.branch", git.getRepository().getBranch()))
                                .and(append("git.state", git.getRepository().getRepositoryState().toString()));

                        log.info(marker, "git push em {}", branch);
                    }));

        } catch (GitAPIException e) {
            log.error(append("push.branch", branch), "git push falhou", e);
        }
    }

    @SneakyThrows
    public void remove(Path caminho) {
        git.rm().addFilepattern(caminho.toString()).call();

        LogstashMarker marker = append("git.state", git.getRepository().getRepositoryState().toString())
                .and(append("git.branch", git.getRepository().getBranch()));

        log.debug(marker, "git rm {}", caminho);
    }
}
