# 🚀 Guia Rápido - Integração Frontend com Sistema 2FA

## 📋 **TL;DR - O que mudou:**
- ✅ Sistema de 2FA implementado no backend
- ✅ Novos campos na resposta de login (`requiresToken`)
- ✅ Novos endpoints para validação de token
- ✅ Fluxo de primeiro acesso com token obrigatório

---

## 🎯 **Principais Mudanças para o Frontend**

### **1. 🔑 Login Agora Pode Retornar `requiresToken: true`**

**Antes:**
```json
// Login sempre retornava tokens diretamente
{
  "accessToken": "jwt...",
  "token": "refresh...",
  "userDetails": {...}
}
```

**Agora:**
```json
// Login pode exigir 2FA
{
  "accessToken": null,
  "token": null,
  "userDetails": {...},
  "requiresToken": true  // ← NOVO!
}
```

### **2. 🔐 Primeiro Acesso Pode Exigir Token**

**Fluxo de definição de senha:**
```json
// Após definir senha no primeiro acesso
{
  "success": true,
  "message": "Senha definida com sucesso",
  "requiresToken": true  // ← Pode exigir token!
}
```

---

## 🔄 **Fluxos que o Frontend Precisa Implementar**

### **🔐 Fluxo 1: Login com 2FA**

```javascript
// 1. Login normal
const loginResponse = await fetch('/api/v1/auth/login', {
  method: 'POST',
  body: JSON.stringify({ username, password })
});

const loginData = await loginResponse.json();

if (loginData.requiresToken === true) {
  // 2. Precisa de token 2FA
  showTokenScreen(loginData.userDetails);
  
  // 3. Usuário digita o token recebido por email/SMS
  const tokenResponse = await fetch('/api/v1/auth/validate-token', {
    method: 'POST',
    body: JSON.stringify({
      userId: loginData.userDetails.user.id,
      token: userInputToken
    })
  });
  
  const tokenData = await tokenResponse.json();
  
  if (tokenResponse.ok) {
    // 4. Login automático realizado!
    saveTokens(tokenData.accessToken, tokenData.token);
    redirectToDashboard();
  } else {
    showError(tokenData.error); // "TOKEN_INVALIDO" ou "TOKEN_EXPIRADO"
  }
} else {
  // Login direto (sem 2FA)
  saveTokens(loginData.accessToken, loginData.token);
  redirectToDashboard();
}
```

### **🏠 Fluxo 2: Primeiro Acesso**

```javascript
// 1. Usuário define senha
const resetResponse = await fetch('/api/auth/password-reset/reset', {
  method: 'POST',
  body: JSON.stringify({ token, newPassword, confirmPassword })
});

const resetData = await resetResponse.json();

if (resetData.requiresToken === true) {
  // 2. Primeiro acesso exige token 2FA
  showMessage('Senha definida! Verifique seu email para o código de segurança.');
  showTokenScreen({ user: { id: extractUserIdFromToken(token) } });
  
  // 3. Usuário digita token recebido
  const tokenResponse = await fetch('/api/v1/auth/validate-token', {
    method: 'POST',
    body: JSON.stringify({
      userId: extractUserIdFromToken(token),
      token: userInputToken
    })
  });
  
  if (tokenResponse.ok) {
    const tokenData = await tokenResponse.json();
    // 4. Login automático após primeiro acesso!
    saveTokens(tokenData.accessToken, tokenData.token);
    redirectToDashboard();
  }
} else {
  // Login automático direto
  saveTokens(resetData.accessToken, resetData.refreshToken);
  redirectToDashboard();
}
```

---

## 📱 **Telas que Precisam ser Criadas/Modificadas**

### **1. 🔑 Tela de Login (Modificar)**
- Adicionar verificação de `requiresToken`
- Redirecionar para tela de token quando necessário

### **2. 🆕 Tela de Token 2FA (Nova)**
- Input para código de 5 dígitos
- Botão "Validar"
- Botão "Reenviar código" (opcional)
- Timer de 5 minutos
- Mensagens de erro específicas

### **3. 🏠 Tela de Primeiro Acesso (Modificar)**
- Após definir senha, verificar `requiresToken`
- Redirecionar para tela de token se necessário

---

## 🔗 **Endpoints para o Frontend**

### **Endpoints Existentes (Modificados):**
```javascript
// ✅ JÁ EXISTEM - apenas adicionaram campo 'requiresToken'
POST /api/v1/auth/login
POST /api/auth/password-reset/reset
```

### **Novos Endpoints:**
```javascript
// 🆕 NOVOS - precisam ser implementados
POST /api/v1/auth/validate-token
POST /api/v1/auth/generate-token  // (opcional - para reenviar)
```

---

## 💻 **Código de Exemplo - Componente React**

### **Componente TokenValidation.jsx**
```jsx
import React, { useState, useEffect } from 'react';

function TokenValidation({ userDetails, onSuccess, onError }) {
  const [token, setToken] = useState('');
  const [loading, setLoading] = useState(false);
  const [timeLeft, setTimeLeft] = useState(300); // 5 minutos

  useEffect(() => {
    const timer = setInterval(() => {
      setTimeLeft(prev => prev > 0 ? prev - 1 : 0);
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    try {
      const response = await fetch('/api/v1/auth/validate-token', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          userId: userDetails.user.id,
          token: token
        })
      });

      if (response.ok) {
        const data = await response.json();
        onSuccess(data);
      } else {
        const error = await response.json();
        onError(error.error === 'TOKEN_EXPIRADO' 
          ? 'Token expirado. Solicite um novo.' 
          : 'Token inválido. Verifique o código.');
      }
    } catch (error) {
      onError('Erro de conexão. Tente novamente.');
    } finally {
      setLoading(false);
    }
  };

  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="token-validation">
      <h2>Código de Segurança</h2>
      <p>Digite o código de 5 dígitos enviado para seu email</p>
      
      <form onSubmit={handleSubmit}>
        <input
          type="text"
          value={token}
          onChange={(e) => setToken(e.target.value.toUpperCase())}
          placeholder="Digite o código"
          maxLength={5}
          pattern="[A-Za-z0-9]{5}"
          required
        />
        
        <button type="submit" disabled={loading || token.length !== 5}>
          {loading ? 'Validando...' : 'Validar'}
        </button>
      </form>

      <div className="timer">
        Tempo restante: {formatTime(timeLeft)}
      </div>
      
      {timeLeft === 0 && (
        <button onClick={() => window.location.reload()}>
          Solicitar novo código
        </button>
      )}
    </div>
  );
}

export default TokenValidation;
```

---

## ⚠️ **Pontos de Atenção**

### **1. 🔍 Verificar `requiresToken` sempre**
```javascript
// ❌ ERRADO - assumir que sempre tem token
const { accessToken } = await login();

// ✅ CORRETO - verificar se precisa de 2FA
const response = await login();
if (response.requiresToken) {
  showTokenScreen();
} else {
  saveTokens(response.accessToken);
}
```

### **2. 🎯 Tratar Erros Específicos**
```javascript
// Erros possíveis na validação:
// - "TOKEN_INVALIDO" → Código digitado errado
// - "TOKEN_EXPIRADO" → Passou de 5 minutos
```

### **3. 🔄 Estados de Loading**
- Mostrar loading durante validação
- Desabilitar botão enquanto processa
- Timer visual de 5 minutos

### **4. 📱 UX Recommendations**
- Input automático de maiúsculas
- Máximo 5 caracteres
- Mensagens claras de erro
- Opção de reenviar código

---

## 🧪 **Como Testar**

### **Cenário 1: Login sem 2FA**
1. Login normal → Deve funcionar como antes

### **Cenário 2: Login com 2FA**
1. Login → `requiresToken: true`
2. Tela de token → Input de 5 dígitos
3. Validar → Login automático

### **Cenário 3: Primeiro Acesso com 2FA**
1. Definir senha → `requiresToken: true`
2. Tela de token → Input de 5 dígitos  
3. Validar → Login automático

---

## 🚀 **Checklist de Implementação**

### **Backend (✅ Pronto)**
- [x] Endpoints de 2FA implementados
- [x] Integração com roles configurada
- [x] Validação de tokens funcionando

### **Frontend (📋 Pendente)**
- [ ] Modificar tela de login
- [ ] Criar tela de token 2FA
- [ ] Modificar fluxo de primeiro acesso
- [ ] Implementar tratamento de erros
- [ ] Adicionar estados de loading
- [ ] Testar todos os cenários

---

## 📞 **Suporte**

**Dúvidas sobre integração?**
- 📋 Collection Postman: `/docs/postman_collection_auth_2fa.json`
- 📖 Documentação completa: `/docs/frontend_integration_guide.md`
- 🔧 Exemplos de código: Neste documento

**Endpoints para testar:**
- Base URL: `http://localhost:8080`
- Todos os endpoints estão funcionando e testados

---

*Sistema 2FA implementado e pronto para integração! 🎉*