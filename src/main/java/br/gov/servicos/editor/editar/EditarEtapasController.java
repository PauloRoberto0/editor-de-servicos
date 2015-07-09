package br.gov.servicos.editor.editar;

import br.gov.servicos.editor.servicos.Etapa;
import br.gov.servicos.editor.servicos.Servico;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@Controller
@FieldDefaults(level = PRIVATE, makeFinal = true)
@RequestMapping(value = "/editar/servico", method = POST)
public class EditarEtapasController {

    private SalvarController salvar;

    @Autowired
    public EditarEtapasController(SalvarController salvar) {
        this.salvar = salvar;
    }

    @RequestMapping(params = {"adicionarEtapa"})
    RedirectView adicionarEtapa(
            Servico servico,
            @AuthenticationPrincipal User usuario
    ) throws IOException {
        servico.getEtapas().add(new Etapa());
        return salvar.salvar(servico, usuario);
    }

    @RequestMapping(params = {"removerEtapa"})
    RedirectView removerEtapa(
            Servico servico,
            @RequestParam("removerEtapa") int indice,
            @AuthenticationPrincipal User usuario
    ) throws IOException {
        servico.getEtapas().remove(indice);
        return salvar.salvar(servico, usuario);
    }

}