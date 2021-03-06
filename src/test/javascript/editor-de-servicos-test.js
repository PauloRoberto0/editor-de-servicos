'use strict';

var xhr = require('lib/mithril-fake-xhr')(window);
var editor = require('servico/editor');

describe('editor', function () {

  beforeEach(function () {
    xhr.reset();

    m.route.param = function () {
      return 'ola-mundo';
    };

  });

  it('deve ser um modulo do mithril', function () {
    expect(editor.controller).toBeDefined();
    expect(editor.view).toBeDefined();
  });

  it('deve inicializar um servico', function () {
    xhr('GET', '/editar/api/pagina/servico/ola-mundo').respondWith('<servico/>');

    var ctrl = new editor.controller();

    expect(ctrl.servico()).toBeDefined();
  });

  it('deve salvar um servico', function () {
    xhr('GET', '/editar/api/id-unico/ola-mundo').respondWith('true');
    xhr('GET', '/editar/api/pagina/servico/ola-mundo').respondWith('<servico/>');
    xhr('POST', '/editar/api/pagina/servico/ola-mundo').respondWith('<servico/>');

    var ctrl = new editor.controller();
    expect(ctrl.servico()).toBeDefined();

    ctrl.servico().nome('olá, mundo!');
    ctrl.servico().descricao('descrição');
    ctrl.servico().palavrasChave(['a', 'b', 'c']);

    expect(ctrl.salvar).toBeDefined();

    ctrl.salvar();

    expect(xhr.unexpectedRequests).toBe(0);
  });

});
