# Documentação Swagger - Fluxo de Autenticação 2FA

Este documento descreve a implementação da documentação Swagger para o fluxo de autenticação de dois fatores (2FA) no sistema.

## Endpoints Documentados

### 1. Geração de Token 2FA
- **Endpoint**: `POST /api/v1/auth/generate-token`
- **Descrição**: Gera um token de autenticação de dois fatores para um usuário específico
- **Request Body**: `GenerateTokenRequest`
- **Response**: `GenerateTokenResponse`

### 2. Validação de Token 2FA
- **Endpoint**: `POST /api/v1/auth/validate-token`
- **Descrição**: Valida um token 2FA e retorna JWT completo se válido
- **Request Body**: `ValidateTokenRequest`
- **Response**: `JwtResponseDTO` (sucesso) ou `ValidateTokenError` (erro)

## DTOs Documentados

### Request DTOs
1. **GenerateTokenRequest**
   - `userId`: ID do usuário (Long, obrigatório)

2. **ValidateTokenRequest**
   - `userId`: ID do usuário (Long, obrigatório)
   - `token`: Token 2FA (String, 5-10 caracteres, obrigatório)

### Response DTOs
1. **GenerateTokenResponse**
   - `token`: Token gerado (String)
   - `expiresAt`: Data de expiração (String, ISO 8601)

2. **ValidateTokenError**
   - `error`: Código do erro (TOKEN_INVALIDO, TOKEN_EXPIRADO, ERRO_INTERNO)

3. **JwtResponseDTO**
   - `accessToken`: Token JWT de acesso
   - `token`: Token de refresh
   - `userDetails`: Dados completos do usuário
   - `requiresToken`: Indica se precisa de token 2FA

4. **AuthResponseDTO**
   - `user`: Dados do usuário
   - `permissions`: Lista de permissões
   - `functionalities`: Lista de funcionalidades

## Entidades Documentadas

### 1. UserToken
Representa um token de autenticação 2FA no banco de dados:
- `id`: Identificador único
- `user`: Usuário proprietário
- `token`: Código do token (máx. 10 caracteres)
- `createdAt`: Data de criação
- `expiresAt`: Data de expiração
- `isValid`: Status de validade

### 2. TwoFactorAuth
Representa as configurações de 2FA de um usuário:
- `id`: Identificador único
- `userId`: ID do usuário
- `twoFactorType`: Tipo de 2FA (SMS/EMAIL)
- `isMandatory`: Se é obrigatório
- `temporaryToken`: Token temporário
- `expiresAt`: Expiração do token temporário
- `createdAt`: Data de criação
- `updatedAt`: Data de atualização

### 3. TwoFactorTypeEnum
Enum com os tipos de 2FA disponíveis:
- `SMS`: Autenticação via SMS
- `EMAIL`: Autenticação via Email

## Exemplos de Uso

### Gerar Token 2FA
```json
POST /api/v1/auth/generate-token
{
  "userId": 1
}

Response:
{
  "token": "A1B2C",
  "expiresAt": "2024-01-15T10:30:00"
}
```

### Validar Token 2FA
```json
POST /api/v1/auth/validate-token
{
  "userId": 1,
  "token": "A1B2C"
}

Response (Sucesso):
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token": "refresh-token-string",
  "userDetails": {
    "user": {
      "id": 1,
      "username": "usuario@exemplo.com",
      "email": "usuario@exemplo.com",
      "roles": ["USER"],
      "isEnabled": true
    }
  }
}

Response (Erro):
{
  "error": "TOKEN_INVALIDO"
}
```

## Códigos de Resposta

### Geração de Token
- **200**: Token gerado com sucesso
- **404**: Usuário não encontrado
- **500**: Erro interno do servidor

### Validação de Token
- **200**: Token validado com sucesso
- **401**: Token inválido ou expirado
- **500**: Erro interno durante validação

## Acessando a Documentação

A documentação Swagger estará disponível em:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

## Tag Swagger

Todos os endpoints estão agrupados sob a tag:
**"Autenticação 2FA"** - Endpoints para geração e validação de tokens de autenticação de dois fatores (2FA)

## Observações

1. Todas as anotações Swagger foram implementadas seguindo as melhores práticas
2. Exemplos realistas foram fornecidos para todos os campos
3. Descrições detalhadas foram adicionadas para facilitar o entendimento
4. Códigos de erro específicos foram documentados
5. Validações de tamanho e obrigatoriedade foram especificadas
6. As entidades do banco de dados também foram documentadas para referência

A documentação está completa e pronta para uso pelos desenvolvedores e consumidores da API.