'use strict';

var Tooltips = require('tooltips');
var EditorBase = require('componentes/editor-base');

module.exports = {

  controller: function (args) {
    this.tipo = m.prop('');

    this.atualizarRota = function () {
      if (this.tipo()) {
        m.route('/editar/' + this.tipo() + '/novo');
      }
    };
  },

  view: function (ctrl, args) {

    return m.component(EditorBase, {

      cabecalhoConfig: {
        metadados: false,
        nomeDaPagina: 'Nova página',
        logout: true,
        salvar: _.noop,
        cabecalho: args.cabecalho
      },

      componentes: [
        m.component(require('pagina/componentes/tipo-de-pagina'), {
          tipo: ctrl.tipo,
          tooltipTipo: Tooltips.tipoPagina,
          novo: !ctrl.tipo(),
          change: function () {
            ctrl.atualizarRota();
          }
        }),
      ]
    });
  }
};
