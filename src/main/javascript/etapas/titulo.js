'use strict';

module.exports = {

  controller: function (args) {
    this.titulo = args.titulo;
    this.indice = args.indice;
  },

  view: function (ctrl) {
    return m('.titulo', [
      m('h3', [
        'Título da etapa ' + (ctrl.indice + 1),
        m.component(require('tooltips').tituloDaEtapa)
      ]),

      m('input[type=text]', {
        onkeyup: m.withAttr('value', ctrl.titulo),
        value: ctrl.titulo()
      })
    ]);
  }
};
