'use strict';

var Tooltips = require('tooltips');
var EditorPagina = require('pagina/editor');

module.exports = {
  view: function () {
    return m.component(EditorPagina, {
      tipo: 'pagina-especial',
      tituloNome: 'Nome da página especial',
      componenteNome: require('componentes/input'),
      tamanhoConteudo: 10000,
      tooltips: {
        tipo: Tooltips.tipoPagina,
        nome: Tooltips.nomePaginaEspecial,
        conteudo: Tooltips.conteudoPaginaEspecial
      }
    });
  }
};
