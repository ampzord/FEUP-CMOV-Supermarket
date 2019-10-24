const request = require('request-promise')
const _ = require('lodash')
const moment = require('moment')

function rawQuery(bearer, query) {
  const URL = 'query'
  return request.post({
    url: URL,
    body: JSON.stringify(query),
    headers: { 
      'Authorization': 'Bearer ' + bearer,
      'Content-Type': 'application/json'
    }
  })
}

function clientes(bearer, primavera_id) {
  const query = `
    SELECT 
      Cliente, Nome, Fac_Mor, Pais 
    FROM Clientes 
    WHERE Vendedor = '${primavera_id}'
  `
  return rawQuery(bearer, query)
}

function cliente(bearer, id) {
  const URL = process.env.API_URL + '/Base/Clientes/Edita/' + id
  return request.get({
    url: URL,
    headers: { 
      'Authorization': 'Bearer ' + bearer
    }
  })
}

function cliente_oportunidades(bearer, id) {
  const query = `
    SELECT
      IdOportunidade,
      NumProposta,
      Entidade,
      Oportunidade
    FROM PropostasOPV
    JOIN CabecOportunidadesVenda ON PropostasOPV.IdOportunidade = CabecOportunidadesVenda.ID
    WHERE Entidade = '${id}'
  `

  return rawQuery(bearer, query)
}

function cria_cliente(bearer, cliente) {
  const URL = process.env.API_URL + '/Base/Clientes/Actualiza'
  return request.post({
    url: URL,
    body: JSON.stringify({
      ...cliente
    }),
    headers: { 
      'Authorization': 'Bearer ' + bearer,
      'Content-Type': 'application/json'
    }
  })
}


module.exports = {
  query: rawQuery,
  clientes,
  cliente,
  cria_cliente,
}