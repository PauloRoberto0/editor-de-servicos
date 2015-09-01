'use strict';

var validacoes = require('validacoes');
var v = require('validacoes');

var id = (function () {
  var counters = {};
  var gerador = function (base) {
    if (!counters[base]) {
      counters[base] = 0;
    }
    return base + '-' + counters[base]++;
  };

  gerador.reset = function () {
    counters = {};
  };

  return gerador;
})();

var Cabecalho = function () {
  this.metadados = m.prop({});
  this.erro = m.prop(null);

  this.limparErro = function () {
    this.erro(null);
  };

  this.tentarNovamente = function (fn) {
    this.erro({
      tentarNovamente: fn
    });
  };
};

var Caso = function (parentId, config) {
  var data = (config || {});
  this.id = id((parentId ? parentId + '-' : '') + 'caso');
  this.padrao = data.padrao;
  this.descricao = m.prop(data.descricao || '');
  this.campos = m.prop(data.campos || []);
};

var CanaisDePrestacao = function (config) {
  var data = (config || {});
  this.id = id('canais-de-prestacao');
  this.casoPadrao = m.prop(data.casoPadrao || new Caso(this.id, {
    padrao: true,
    campos: []
  }));
  this.outrosCasos = m.prop(data.outrosCasos || []);
};

var CanalDePrestacao = function (config) {
  var data = (config || {});
  this.id = id('canal-de-prestacao');
  this.tipo = m.prop(data.tipo || '');
  this.descricao = m.prop(data.descricao || '');
};

var Documentos = function (config) {
  var data = (config || {});
  this.id = id('documentos');
  this.casoPadrao = m.prop(data.casoPadrao || new Caso(this.id, {
    padrao: true,
    campos: []
  }));
  this.outrosCasos = m.prop(data.outrosCasos || []);
};

var Custo = function (config) {
  var data = (config || {});
  this.id = id('custo');
  this.descricao = m.prop(data.descricao || '');
  this.moeda = m.prop(data.moeda || '');
  this.valor = m.prop(data.valor || '');
};

var Custos = function (config) {
  var data = (config || {});
  this.id = id('custos');
  this.casoPadrao = m.prop(data.casoPadrao || new Caso(this.id, {
    padrao: true,
    campos: []
  }));
  this.outrosCasos = m.prop(data.outrosCasos || []);
};

var Etapa = function (config) {
  var data = (config || {});
  this.id = id('etapa');
  this.titulo = v.prop(data.titulo || '', v.maximo(100));
  this.descricao = v.prop(data.descricao || '', v.maximo(500));
  this.documentos = m.prop(data.documentos || new Documentos());
  this.custos = m.prop(data.custos || new Custos());
  this.canaisDePrestacao = m.prop(data.canaisDePrestacao || new CanaisDePrestacao());

  this.validador = new m.validator(validacoes.Etapa);
  this.validar = function () {
    this.validador = new m.validator(validacoes.Etapa);
    this.validador.validate(this);
  };
};

var Solicitante = function (config) {
  var data = (config || {});
  this.id = id('solicitante');
  this.tipo = m.prop(data.tipo || '');
  this.requisitos = m.prop(data.requisitos || '');

  this.validador = new m.validator(validacoes.Solicitante);
  this.validar = function () {
    this.validador = new m.validator(validacoes.Solicitante);
    this.validador.validate(this);
  };
};

var TempoTotalEstimado = function (config) {
  var data = (config || {});
  this.id = id('tempo-total-estimado');
  this.tipo = m.prop(data.tipo || '');
  this.entreMinimo = m.prop(data.entreMinimo || '');
  this.ateMaximo = m.prop(data.ateMaximo || '');
  this.ateTipoMaximo = m.prop(data.ateTipoMaximo || '');
  this.entreMaximo = m.prop(data.entreMaximo || '');
  this.entreTipoMaximo = m.prop(data.entreTipoMaximo || '');
  this.descricao = m.prop(data.descricao || '');

  this.validador = new m.validator(validacoes.TempoTotalEstimado);
  this.validar = function () {
    this.validador = new m.validator(validacoes.TempoTotalEstimado);
    this.validador.validate(this);
  };
};

var Servico = function (config) {
  var data = (config || {});
  this.id = id('servico');
  this.nome = v.prop(data.nome || '', v.obrigatorio, v.maximo(150));
  this.sigla = v.prop(data.sigla || '', v.maximo(15));
  this.nomesPopulares = v.prop(data.nomesPopulares || [], v.cada(v.maximo(150)));
  this.descricao = v.prop(data.descricao || '', v.obrigatorio, v.maximo(500));
  this.gratuidade = m.prop(data.gratuidade);
  this.solicitantes = v.prop(data.solicitantes || [], v.minimo(1));
  this.tempoTotalEstimado = m.prop(data.tempoTotalEstimado || new TempoTotalEstimado());
  this.etapas = v.prop(data.etapas || [], v.minimo(1));
  this.orgao = m.prop(data.orgao || '');
  this.segmentosDaSociedade = v.prop(data.segmentosDaSociedade || [], v.minimo(1));
  this.areasDeInteresse = v.prop(data.areasDeInteresse || [], v.minimo(1));
  this.palavrasChave = v.prop(data.palavrasChave || [], v.cada(v.maximo(50)), v.minimo(3));
  this.legislacoes = m.prop(data.legislacoes || []);

  this.validador = new m.validator(validacoes.Servico);
  this.validar = function () {
    this.validador = new m.validator(validacoes.Servico);
    this.validador.validate(this);
  };
};

module.exports = {
  id: id,
  Cabecalho: Cabecalho,
  Caso: Caso,
  CanaisDePrestacao: CanaisDePrestacao,
  CanalDePrestacao: CanalDePrestacao,
  Documentos: Documentos,
  Custo: Custo,
  Custos: Custos,
  Etapa: Etapa,
  Solicitante: Solicitante,
  Servico: Servico,
  TempoTotalEstimado: TempoTotalEstimado
};
