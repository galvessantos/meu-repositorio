### História do Usuário - Criação de Novo Usuário com Cadastro de Senha e Autenticação

---

**Título:**  
Criação e Ativação de Novo Usuário com Autenticação por Token

---

**Como**: Administrador do sistema  
**Eu quero**: Criar novos usuários e garantir sua ativação de forma segura  
**Para que**: Os usuários tenham acesso ao sistema de maneira controlada e segura

---

### **Cenário 1: Criação de Novo Usuário**

**Dado que** o administrador está logado no sistema  
**Quando** ele acessa a funcionalidade de criação de usuários  
**E** preenche os campos obrigatórios:
- Nome
- CPF
- E-mail
- Telefone
- Login
- Perfil
- Empresa vinculada  
  **Então** o sistema deve salvar as informações e enviar um e-mail para o novo usuário com instruções de cadastro de senha e um link de ativação.

---

### **Cenário 2: Cadastro de Senha e Token**

**Dado que** o novo usuário recebe o e-mail de ativação  
**Quando** ele acessa o link informado  
**Então** o sistema deve exibir uma tela com os seguintes campos:
- Senha
- Confirmação de senha
- Seleção de envio do token (SMS ou E-mail)
- Campo para inserir o token recebido

**E** deve permitir ao usuário:
1. Solicitar o envio de um token temporário para o canal escolhido (SMS ou E-mail).
2. Cadastrar a nova senha e confirmar o token.

**E** o sistema deve:
- Validar se o token é válido e ainda não expirou.
- Exibir mensagem de erro caso o token seja inválido ou expirado.

---

### **Cenário 3: Sucesso no Cadastro**

**Dado que** o usuário preenche corretamente a senha, confirmação de senha e token  
**Quando** ele conclui o cadastro com sucesso  
**Então** o sistema deve:
- Atualizar o registro do usuário, marcando o campo `isPasswordChangedByUser` como **true**.
- Redirecioná-lo para a tela de login com uma mensagem de sucesso.

---

### **Cenário 4: Falha no Cadastro**

**Dado que** o usuário tenta cadastrar a senha com um token inválido ou expirado  
**Quando** ele submete o formulário  
**Então** o sistema deve exibir uma mensagem clara de erro:
- "Token inválido ou expirado. Solicite um novo token e tente novamente."

---

### **Requisitos Técnicos**

- Adicionar os campos `token_temporary` e `token_expired_at` na tabela de autenticação.
- Garantir que o token seja gerado com validade configurável (ex.: 15 minutos).
- Implementar lógica para reenvio do token via SMS ou e-mail.
- Garantir que o sistema valide o token e senha no momento do cadastro.
