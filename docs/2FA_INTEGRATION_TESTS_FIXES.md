# Correções dos Testes de Integração 2FA

Este documento descreve as correções implementadas para resolver os erros nos testes de integração do `AuthTokenControllerIntegrationTest`.

## 🐛 Erros Identificados e Corrigidos

### 1. ✅ **Erro de Import - AutoConfigureTestDatabase**

**Problema**:
```java
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase; // ❌ ERRADO
```

**Correção**:
```java
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase; // ✅ CORRETO
```

### 2. ✅ **Erro de Serialização Jackson**

**Problema**: `GenerateTokenResponse` e `ValidateTokenError` com campos `final` não podiam ser deserializadas.

**Correção**: Adicionadas anotações Jackson:
```java
@Data
public static class GenerateTokenResponse {
    private final String token;
    private final String expiresAt;
    
    @JsonCreator
    public GenerateTokenResponse(
        @JsonProperty("token") String token,
        @JsonProperty("expiresAt") String expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }
}

@Data
public static class ValidateTokenError {
    private final String error;
    
    @JsonCreator
    public ValidateTokenError(@JsonProperty("error") String error) {
        this.error = error;
    }
}
```

### 3. ✅ **Token Muito Longo**

**Problema**: Token `"FIRST_LOGIN"` (11 chars) excedia limite VARCHAR(10).

**Correção**:
```java
// ❌ ANTES
.token("FIRST_LOGIN")  // 11 caracteres
request.setToken("FIRST_LOGIN");

// ✅ DEPOIS  
.token("FIRST1")       // 6 caracteres
request.setToken("FIRST1");
```

### 4. ✅ **Campos Obrigatórios Faltando**

**Problema**: `anotherUser` criado sem todos os campos obrigatórios.

**Correção**: Adicionados todos os campos necessários:
```java
anotherUser.setCpf("98765432100");
anotherUser.setPhone("+5511888888888");
anotherUser.setCompanyId("456");
anotherUser.setEnabled(true);
anotherUser.setFirstLoginCompleted(true);
anotherUser.setPasswordChangedByUser(true);
anotherUser.setCreatedByAdmin(false);
anotherUser.setReset(false);
```

### 5. ✅ **Validação de Request**

**Problema**: Controller não validava requests nulos/vazios, causando 500 em vez de 400.

**Correção**: Adicionada validação nos endpoints:
```java
// generateToken
if (req.getUserId() == null) {
    return ResponseEntity.badRequest().build();
}

// validateToken  
if (req.getUserId() == null || req.getToken() == null || req.getToken().trim().isEmpty()) {
    return ResponseEntity.badRequest().body(new ValidateTokenError("DADOS_INVALIDOS"));
}
```

### 6. ✅ **Problema de Transação**

**Problema**: Método `getActiveToken()` com transação de escrita causando `TransactionRequiredException`.

**Correção**: Separados os métodos:
```java
@Transactional(readOnly = true)
public Optional<UserToken> getActiveToken(Long userId) {
    // Apenas leitura
}

@Transactional  
public Optional<UserToken> cleanupDuplicateTokens(Long userId, LocalDateTime now) {
    // Escrita em transação separada
}
```

### 7. ✅ **Persistência em Testes**

**Problema**: `assertFalse(oldTokenFromDb.getIsValid())` falhando porque mudanças não eram persistidas.

**Correção**: Adicionado flush explícito:
```java
// Flush para garantir que as mudanças sejam persistidas
userTokenRepository.flush();
var oldTokenFromDb = userTokenRepository.findById(oldToken.getId()).orElse(null);
```

### 8. ✅ **Status Codes Atualizados**

**Problema**: Testes esperavam status específicos mas recebiam 500.

**Correção**: Ajustadas expectativas após validações:
```java
// validateToken_EmptyRequest
.andExpect(status().isBadRequest())           // Era isUnauthorized()
.andExpect(jsonPath("$.error", is("DADOS_INVALIDOS"))); // Era "TOKEN_INVALIDO"
```

## 📊 Resumo das Correções

| Erro | Antes | Depois | Status |
|------|-------|--------|--------|
| **Import** | `web.servlet.AutoConfigureTestDatabase` | `jdbc.AutoConfigureTestDatabase` | ✅ |
| **Serialização** | Sem `@JsonCreator` | Com `@JsonCreator/@JsonProperty` | ✅ |
| **Token Length** | `"FIRST_LOGIN"` (11) | `"FIRST1"` (6) | ✅ |
| **Campos Obrigatórios** | CPF/Phone faltando | Todos preenchidos | ✅ |
| **Validação** | Sem validação → 500 | Com validação → 400 | ✅ |
| **Transação** | Read-write conflict | Métodos separados | ✅ |
| **Persistência** | Sem flush | Com flush explícito | ✅ |
| **Status Codes** | Expectativas incorretas | Alinhadas com validações | ✅ |

## 🎯 Resultados Esperados

### Status Codes Corretos
- ✅ `200` - Operações bem-sucedidas
- ✅ `400` - Requests inválidos (userId null, token vazio)
- ✅ `401` - Tokens inválidos/expirados
- ✅ `404` - Usuário não encontrado
- ✅ `500` - Apenas erros internos reais

### Serialização Funcionando
- ✅ `GenerateTokenResponse` serializa/deserializa corretamente
- ✅ `ValidateTokenError` serializa/deserializa corretamente
- ✅ Testes end-to-end funcionam

### Persistência Confiável
- ✅ Tokens antigos são invalidados
- ✅ Mudanças são persistidas com flush
- ✅ Transações funcionam corretamente

## 🚀 Execução dos Testes

```bash
# Executar testes de integração do controller
./mvnw test -Dtest="AuthTokenControllerIntegrationTest"

# Verificar se todos passam
./mvnw test -Dtest="**/oauth/**/*IntegrationTest"
```

Todas as correções foram implementadas para garantir que os testes de integração passem sem erros! 🎉