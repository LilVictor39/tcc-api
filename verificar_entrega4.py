#!/usr/bin/env python3
"""Verificacao real via HTTPS; grava apenas resultados e nunca credenciais/tokens."""
import csv,json,ssl,sys,time,uuid
from pathlib import Path
from urllib.request import Request,urlopen
from urllib.error import HTTPError
base=(sys.argv[1] if len(sys.argv)>1 else 'https://localhost:8443').rstrip('/')
cert=sys.argv[2] if len(sys.argv)>2 else 'certs/localhost.crt'
if not base.startswith('https://'):raise SystemExit('Use uma URL HTTPS nesta verificacao.')
context=ssl.create_default_context(cafile=cert)
rows=[]
def check(name,expected,observed):
 ok=expected==observed;rows.append([name,str(expected),str(observed),'PASSOU' if ok else 'FALHOU'])
 print(('PASSOU: ' if ok else 'FALHOU: ')+name)
 if not ok:raise AssertionError(f'{name}: esperado {expected}; observado {observed}')
def req(method,path,data=None,token=None,raw=None,ctype='application/json'):
 headers={'Accept':'application/json'}
 if token:headers['Authorization']='Bearer '+token
 body=raw if raw is not None else (json.dumps(data).encode() if data is not None else None)
 if body is not None:headers['Content-Type']=ctype
 request=Request(base+path,data=body,method=method,headers=headers)
 try:
  with urlopen(request,context=context,timeout=15) as response:code=response.status;h=response.headers;rawbody=response.read()
 except HTTPError as e:code=e.code;h=e.headers;rawbody=e.read()
 try:parsed=json.loads(rawbody) if rawbody else None
 except ValueError:parsed=None
 return code,parsed,h,rawbody.decode(errors='replace')
def error(name,expected,result):
 code,body,headers,raw=result;check(name,expected,code)
 check(name+' - erro JSON padronizado',True,isinstance(body,dict) and body.get('status')==expected and all(k in body for k in ['timestamp','error','message','fields']))
 check(name+' - sem detalhes internos',False,any(k in raw for k in ['stackTrace','org.springframework','SQLException','senhaHash','java.lang']))
 return result
try:
 error('Sem token',401,req('GET','/api/tarefas'))
 check('HSTS presente',True,'max-age=' in req('GET','/api/tarefas')[2].get('Strict-Transport-Security',''))
 tokens=[]
 for label in ['a','b']:
  creds={'email':f'entrega4.{label}.{uuid.uuid4().hex[:12]}@example.test','senha':'TesteEntrega4_2026!'}
  check('Cadastro '+label,201,req('POST','/api/auth/cadastro',creds)[0])
  result=req('POST','/api/auth/login',creds);check('Login '+label,200,result[0]);tokens.append(result[1]['accessToken'])
 a,b=tokens
 error('Email invalido',400,req('POST','/api/auth/cadastro',{'email':'invalido','senha':'TesteEntrega4_2026!'}))
 error('Senha Unicode acima de 72 bytes',400,req('POST','/api/auth/login',{'email':'novo@example.test','senha':'á'*37}))
 dados={'titulo':'Tarefa Entrega 4','descricao':'Teste HTTPS','status':'PENDENTE'}
 for name,alter in [('Titulo vazio',{'titulo':'   '}),('Titulo acima de 255',{'titulo':'a'*256}),('Descricao acima de 1000',{'descricao':'a'*1001}),('Status invalido',{'status':'INVALIDO'}),('Proprietario forjado',{'usuarioId':999})]:
  error(name,400,req('POST','/api/tarefas',dados|alter,a))
 error('JSON malformado',400,req('POST','/api/tarefas',token=a,raw=b'{invalido'))
 error('Tipo de conteudo incorreto',415,req('POST','/api/tarefas',token=a,raw=b'texto',ctype='text/plain'))
 error('Corpo acima de 16 KiB',413,req('POST','/api/tarefas',dados|{'descricao':'a'*17000},a))
 result=req('POST','/api/tarefas',dados,a);check('Criacao valida',201,result[0]);task='/api/tarefas/'+str(result[1]['id'])
 for method in ['GET','PUT','DELETE']:error('B bloqueado em '+method,404,req(method,task,dados if method=='PUT' else None,b))
 check('A ainda consulta',200,req('GET',task,token=a)[0])
 error('ID negativo',400,req('GET','/api/tarefas/-1',token=a))
 error('ID nao numerico',400,req('GET','/api/tarefas/abc',token=a))
 error('Metodo nao permitido',405,req('PATCH',task,token=a))
 check('Exclusao pelo dono',204,req('DELETE',task,token=a)[0])
 for _ in range(11):
  result=req('POST','/api/auth/login',{'email':'inexistente@example.test','senha':'SenhaTeste123!'})
  if result[0]==429:break
 error('Limite de autenticacao',429,result);check('Retry-After autenticacao',True,int(result[2].get('Retry-After','0'))>0)
 for _ in range(121):
  result=req('GET','/api/tarefas',token=a)
  if result[0]==429:break
 error('Limite geral da API',429,result);check('Retry-After API',True,int(result[2].get('Retry-After','0'))>0)
 print('Validacao concluida. As duas contas ficticias permanecem no banco. Reinicie a API antes de repetir para limpar os limites em memoria.')
finally:
 with open('evidencias_https_entrega4.csv','w',newline='',encoding='utf-8-sig') as f:
  writer=csv.writer(f,delimiter=';');writer.writerow(['Cenario','Esperado','Observado','Resultado']);writer.writerows(rows)
