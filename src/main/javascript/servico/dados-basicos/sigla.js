'use strict';

module.exports = {

  controller: function (args) {
    this.servico = args.servico;
  },

  view: function (ctrl, args) {
    ctrl.servico = args.servico;

    return m('fieldset#sigla', [
      m('h3.opcional', [
        'Sigla do serviço',
        m.component(require('tooltips').sigla)
      ]),

      m('div.input-container', {
        class: ctrl.servico().sigla.erro()
      }, [
        m('input[type=text]', {
          onchange: m.withAttr('value', ctrl.servico().sigla),
          value: ctrl.servico().sigla()
        })
      ])
    ]);
  }
};
