package br.gov.servicos.editor.conteudo.paginas;

import br.gov.servicos.editor.conteudo.RepositorioGit;
import br.gov.servicos.editor.utils.EscritorDeArquivos;
import br.gov.servicos.editor.utils.LeitorDeArquivos;
import br.gov.servicos.editor.utils.ReformatadorXml;
import com.github.slugify.Slugify;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@NoArgsConstructor
@AllArgsConstructor
public class PaginaVersionadaFactory {

    @Autowired
    Slugify slugify;

    @Autowired
    RepositorioGit repositorio;

    @Autowired
    LeitorDeArquivos leitorDeArquivos;

    @Autowired
    EscritorDeArquivos escritorDeArquivos;

    @Autowired
    ReformatadorXml reformatadorXml;

    @Bean // necessário para @Cacheable
    @Scope("prototype")
    public PaginaVersionada paginaDeOrgao(String texto) {
        return pagina(texto, "orgao");
    }

    @Bean // necessário para @Cacheable
    @Scope("prototype")
    public PaginaVersionada paginaAreaDeInteresse(String texto) {
        return pagina(texto, "area-de-interesse");
    }

    @Bean // necessário para @Cacheable
    @Scope("prototype")
    public PaginaVersionada paginaEspecial(String texto) {
        return pagina(texto, "especial");
    }

    private PaginaVersionada pagina(String texto, String tipo) {
        return new PaginaVersionada(slugify.slugify(texto), tipo, repositorio, leitorDeArquivos, escritorDeArquivos, slugify, reformatadorXml);
    }

}