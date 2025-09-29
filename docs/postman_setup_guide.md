# 🚀 Guia de Configuração - Collection Postman de Autenticação 2FA

## 📁 Como Importar a Collection

### 1. Importar no Postman
1. Abra o Postman
2. Clique em **"Import"** (canto superior esquerdo)
3. Selecione **"Upload Files"**
4. Escolha o arquivo `postman_collection_auth_2fa.json`
5. Clique em **"Import"**

### 2. Configurar Variáveis
Após importar, configure as variáveis da collection:

1. Clique com botão direito na collection **"Sistema de Autenticação e 2FA"**
2. Selecione **"Edit"**
3. Vá na aba **"Variables"**
4. Configure os valores:

```
base_url: http://localhost:8080  (ou sua URL do backend)
username: usuario@teste.com      (usuário existente no sistema)
password: Teste@123             (senha do usuário)
```

---

## 🧪 Como Executar os Testes

### Opção 1: Executar Collection Completa
1. Clique com botão direito na collection
2. Selecione **"Run collection"**
3. Marque todas as pastas que deseja testar
4. Clique em **"Run Sistema de Autenticação e 2FA"**

### Opção 2: Executar por Fluxo
Execute as pastas na ordem recomendada:

1. **🏗️ 0. PREPARAÇÃO DE DADOS** *(Obrigatório - cria usuários de teste)*
2. **🔐 1. FLUXO DE CADASTRO (PRIMEIRO ACESSO)** *(usa usuário recém-criado)*
3. **🔄 2. FLUXO DE RESET DE SENHA** *(usa usuário existente)*
4. **🔑 3. FLUXO DE LOGIN COM 2FA**
5. **🛡️ 4. GERENCIAMENTO DE TOKENS 2FA**
6. **⚠️ 5. TESTES DE CASOS EXTREMOS**
7. **🔄 6. TESTES DE REFRESH TOKEN**
8. **🔒 7. TESTES COM AUTENTICAÇÃO**

> **⚠️ IMPORTANTE**: Execute sempre a pasta "0. PREPARAÇÃO DE DADOS" primeiro para criar os usuários necessários para os testes!

### Opção 3: Executar Requisições Individuais
- Clique em uma requisição específica
- Clique em **"Send"**
- Veja os testes passarem na aba **"Test Results"**

---

## 🔧 Configuração de Ambiente (Opcional)

Para maior flexibilidade, crie um Environment:

### 1. Criar Environment
1. Clique no ícone de **"Environment"** (canto superior direito)
2. Clique em **"+"** para criar novo environment
3. Nome: **"Desenvolvimento 2FA"**

### 2. Configurar Variáveis do Environment
```
Variable Name    | Initial Value           | Current Value
base_url         | http://localhost:8080   | http://localhost:8080
username         | usuario@teste.com       | usuario@teste.com
password         | Teste@123               | Teste@123
```

### 3. Ativar Environment
- Selecione **"Desenvolvimento 2FA"** no dropdown de environments

---

## 🔍 Diferença Entre Primeiro Acesso e Reset de Senha

### ❓ **Por que preciso criar um usuário novo?**

O sistema diferencia entre **PRIMEIRO ACESSO** e **RESET DE SENHA** baseado no campo `isPasswordChangedByUser` do usuário:

- **`isPasswordChangedByUser = false`** → **PRIMEIRO ACESSO** (`firstAccess: true`)
- **`isPasswordChangedByUser = true`** → **RESET DE SENHA** (`firstAccess: false`)

### 🏗️ **Preparação de Dados**
A pasta **"0. PREPARAÇÃO DE DADOS"** é essencial porque:

1. **Cria usuário NOVO** (`isPasswordChangedByUser = false`) para testar primeiro acesso
2. **Verifica usuário EXISTENTE** (`isPasswordChangedByUser = true`) para testar reset

### 📊 **Fluxos Testados**

| Cenário | Usuário | `firstAccess` | Resultado |
|---------|---------|---------------|-----------|
| **Primeiro Acesso** | Recém-criado | `true` | Pode exigir 2FA após definir senha |
| **Reset de Senha** | Existente | `false` | Login automático (sem 2FA) |

> **💡 Dica**: Se você usar um usuário que já definiu senha para testar "primeiro acesso", o sistema retornará `firstAccess: false` e o teste falhará!

---

## 📋 Estrutura da Collection

### 🏗️ **0. PREPARAÇÃO DE DADOS**
- **0.1** Criar Usuário para Primeiro Acesso
- **0.2** Verificar Usuário Existente (Para Reset)

### 🔐 **1. FLUXO DE CADASTRO (PRIMEIRO ACESSO)**
- **1.1** Gerar Token de Reset (Primeiro Acesso)
- **1.2** Validar Token de Reset (Primeiro Acesso)
- **1.3** Definir Senha (Primeiro Acesso - COM 2FA)

### 🔄 **2. FLUXO DE RESET DE SENHA**
- **2.1** Gerar Token de Reset (Usuário Existente)
- **2.2** Validar Token de Reset (Usuário Existente)
- **2.3** Redefinir Senha (Login Automático)

### 🔑 **3. FLUXO DE LOGIN COM 2FA**
- **3.1** Login SEM 2FA (Usuário Normal)
- **3.2** Login COM 2FA (Usuário com Role 2FA)

### 🛡️ **4. GERENCIAMENTO DE TOKENS 2FA**
- **4.1** Gerar Token 2FA Manualmente
- **4.2** Validar Token 2FA (Sucesso)
- **4.3** Validar Token 2FA (Token Inválido)
- **4.4** Validar Token 2FA (Token Já Usado)

### ⚠️ **5. TESTES DE CASOS EXTREMOS**
- **5.1** Validar Token Reset Inválido
- **5.2** Reset com Token Inválido
- **5.3** Reset com Senhas Não Coincidentes
- **5.4** Login com Credenciais Inválidas

### 🔄 **6. TESTES DE REFRESH TOKEN**
- **6.1** Refresh Token Válido
- **6.2** Refresh Token Inválido

### 🔒 **7. TESTES COM AUTENTICAÇÃO**
- **7.1** Acesso a Endpoint Protegido (Com Token)
- **7.2** Acesso a Endpoint Protegido (Sem Token)

---

## 🎯 Testes Automatizados

Cada requisição possui testes automatizados que verificam:

### ✅ **Testes Comuns (Todas as Requisições)**
- Status code correto
- Tempo de resposta < 5 segundos
- Content-Type adequado

### 🔍 **Testes Específicos por Endpoint**

#### **Geração de Tokens**
- Retorna resetLink válido
- Extrai token da URL automaticamente
- Valida formato do token

#### **Validação de Tokens**
- Confirma se token é válido/inválido
- Verifica campo `firstAccess` correto
- Testa mensagens de erro apropriadas

#### **Reset/Definição de Senha**
- Valida sucesso da operação
- Verifica se `requiresToken` está correto
- Confirma login automático quando aplicável
- Salva tokens JWT automaticamente

#### **Login**
- Testa fluxos com e sem 2FA
- Verifica retorno de tokens
- Salva dados do usuário automaticamente

#### **Tokens 2FA**
- Valida formato do token (5 caracteres alfanuméricos)
- Testa expiração e invalidação
- Confirma autenticação após validação
- Verifica uso único dos tokens

#### **Casos de Erro**
- Valida códigos de erro específicos
- Testa mensagens de erro apropriadas
- Confirma que tokens inválidos são rejeitados

---

## 📊 Como Interpretar os Resultados

### ✅ **Testes Passando (Verde)**
- Endpoint funcionando corretamente
- Resposta conforme esperado
- Integração OK

### ❌ **Testes Falhando (Vermelho)**
- Possível bug no backend
- Configuração incorreta
- Dados de teste inadequados

### ⏳ **Variáveis Dinâmicas**
A collection salva automaticamente:
- `reset_token` - Token de reset extraído das URLs
- `two_factor_token` - Token 2FA gerado
- `access_token` - JWT de acesso
- `refresh_token` - Token de refresh
- `user_id` - ID do usuário autenticado

---

## 🔧 Troubleshooting

### **Problema: "Token não encontrado"**
**Solução:**
- Execute os testes na ordem correta
- Certifique-se de que o usuário existe no banco
- Verifique se o backend está rodando

### **Problema: "Unauthorized (401)"**
**Solução:**
- Verifique se o `access_token` foi salvo corretamente
- Execute o login antes de testar endpoints protegidos
- Confirme se o JWT não expirou

### **Problema: "firstAccess sempre null"**
**Solução:**
- Usuário pode não existir no sistema
- Verifique dados de teste no banco
- Confirme se o campo `is_password_changed_by_user` está correto

### **Problema: "Tempo de resposta muito alto"**
**Solução:**
- Verifique performance do backend
- Confirme se banco de dados está otimizado
- Teste em ambiente local primeiro

### **Problema: "2FA não funciona"**
**Solução:**
- Confirme se usuário tem role com `is_token_login = true`
- Verifique se token foi gerado corretamente
- Teste geração manual de token primeiro

---

## 📝 Logs e Debug

### **Console do Postman**
- Abra **"Console"** (View → Show Postman Console)
- Veja logs detalhados de cada requisição
- Tokens e variáveis são logados automaticamente

### **Variáveis de Debug**
```javascript
console.log('Token atual:', pm.collectionVariables.get('two_factor_token'));
console.log('User ID:', pm.collectionVariables.get('user_id'));
console.log('Access Token:', pm.collectionVariables.get('access_token'));
```

### **Scripts Personalizados**
Adicione no **Pre-request Script** ou **Tests**:
```javascript
// Debug de requisição
console.log('Request URL:', pm.request.url);
console.log('Request Body:', pm.request.body);

// Debug de resposta
console.log('Response Status:', pm.response.code);
console.log('Response Body:', pm.response.json());
```

---

## 🎯 Cenários de Teste Cobertos

### **Cenários Positivos ✅**
- [x] Cadastro de usuário (primeiro acesso)
- [x] Reset de senha (usuário existente)
- [x] Login sem 2FA
- [x] Login com 2FA
- [x] Geração de tokens 2FA
- [x] Validação de tokens 2FA
- [x] Refresh de tokens JWT
- [x] Acesso a endpoints protegidos

### **Cenários Negativos ❌**
- [x] Tokens inválidos/expirados
- [x] Senhas não coincidentes
- [x] Credenciais incorretas
- [x] Reutilização de tokens
- [x] Acesso não autorizado
- [x] Refresh tokens inválidos

### **Casos Extremos ⚠️**
- [x] Múltiplas tentativas de validação
- [x] Tokens já utilizados
- [x] Validação de formato de tokens
- [x] Teste de expiração (5 minutos)
- [x] Invalidação automática de tokens anteriores

---

## 📞 Suporte

### **Para Desenvolvedores**
- Consulte logs do console Postman
- Verifique variáveis da collection
- Execute testes individuais para debug

### **Para QA/Testes**
- Execute collection completa regularmente
- Monitore testes que falham consistentemente
- Reporte bugs com logs detalhados

### **Para DevOps**
- Use collection em pipelines CI/CD
- Monitore performance dos endpoints
- Configure alertas para falhas de teste

---

*Collection criada para testar 100% dos fluxos de autenticação e 2FA do sistema.*