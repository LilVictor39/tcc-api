#!/usr/bin/env python3
"""Teste HTTP repetivel no ambiente local; nao imprime tokens nem senhas."""
import json, urllib.request, urllib.error, uuid, csv, sys
from pathlib import Path
base=sys.argv[1].rstrip('/') if len(sys.argv)>1 else 'http://localhost:8080'
rows=[]
def req(method,path,body=None,token=None):
 headers={'Content-Type':'application/json'}
 if token:headers['Authorization']='Bearer '+token
 data=None if body is None else json.dumps(body).encode()
 try:
  with urllib.request.urlopen(urllib.request.Request(base+path,data=data,headers=headers,method=method),timeout=15) as r:
   raw=r.read();return r.status,json.loads(raw) if raw else None
 except urllib.error.HTTPError as e:
  raw=e.read()
  try:parsed=json.loads(raw) if raw else None
  except ValueError:parsed=None
  return e.code,parsed

def check(name,actual,expected):
 rows.append([name,expected,actual,'PASSOU' if actual==expected else 'FALHOU'])
 print(f'{rows[-1][-1]}: {name} (esperado {expected}; observado {actual})')
 if actual!=expected:raise AssertionError(name)
run=uuid.uuid4().hex[:10]
accounts=[]
for label in ('a','b'):
 cred={'email':f'{label}-{run}@example.test','senha':f'Teste-{uuid.uuid4().hex}'}
 code,body=req('POST','/api/auth/cadastro',cred);check('Cadastro '+label,code,201)
 code,body=req('POST','/api/auth/login',cred);check('Login '+label,code,200)
 accounts.append(body['accessToken'])
a,b=accounts
code,_=req('GET','/api/tarefas');check('Sem token',code,401)
code,_=req('GET','/api/tarefas',token='invalido');check('Token invalido',code,401)
dados={'titulo':'Tarefa de teste '+run,'descricao':'Dado ficticio','status':'PENDENTE'}
code,body=req('POST','/api/tarefas',dados,a);check('Criacao por A',code,201)
id=body['id']
try:
 code,_=req('GET',f'/api/tarefas/{id}',token=a);check('Consulta pelo dono',code,200)
 code,lista=req('GET','/api/tarefas',token=b);check('Listagem de B',code,200);check('Isolamento da listagem',any(x['id']==id for x in lista),False)
 for method in ('GET','PUT','DELETE'):
  code,_=req(method,f'/api/tarefas/{id}',dados if method=='PUT' else None,b);check(method+' por outro usuario',code,404)
 code,_=req('PUT',f'/api/tarefas/{id}',dict(dados,usuarioId=999),a);check('Tentativa de trocar dono',code,400)
 code,_=req('PUT',f'/api/tarefas/{id}',dict(dados,titulo='Alterada'),a);check('Alteracao pelo dono',code,200)
 code,_=req('DELETE',f'/api/tarefas/{id}',token=a);check('Exclusao pelo dono',code,204)
finally:
 req('DELETE',f'/api/tarefas/{id}',token=a)
 with Path('evidencias_http_entrega3.csv').open('w',newline='',encoding='utf-8-sig') as f:
  writer=csv.writer(f,delimiter=';');writer.writerow(['Cenario','Esperado','Observado','Resultado']);writer.writerows(rows)
print('Evidencias gravadas em evidencias_http_entrega3.csv. As duas contas ficticias permanecem no banco de testes.')
