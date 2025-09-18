## DOCUMENTACAO DO FLUXO DE REGISTRO DE AUDITORIA


> Estrutura da Entidade de Auditoria:

#### A entidade Audit foi definida com os seguintes atributos:

- userId (Long): Identificador do usuário que realizou a ação.
- action (String): Descrição da ação executada pelo usuário.
- status (RegisterAuditStatusEnum): Enumeração que indica o status da ação (por exemplo, SUCESSO, FALHA).
- description (String): Detalhes adicionais sobre a ação realizada.
- ipAddress (String): Endereço IP de onde a ação foi executada.
- timestamp (LocalDateTime): Data e hora em que a ação ocorreu.

> Fluxo de Registro de Auditoria:

O processo de auditoria é acionado para cada movimentação relevante no sistema, 
como alterações de status, envio de notificações, erros, entre outros. 
O método registerAudit(RegisterAuditDTO registerAuditDTO) do AuditService 
é responsável por registrar essas ações. O fluxo detalhado é o seguinte:

### Interceptação da Requisição:

Um interceptor (ClientIpInterceptor) captura todas as requisições que passam pelo sistema.
Antes que a requisição seja processada pelo controlador, o interceptor chama o 
método IpUtils.getClientIp(request) para obter o endereço IP do cliente e armazena 
esse IP em uma variável ThreadLocal através do ClientIpHolder.
Processamento da Requisição:

O controlador correspondente processa a requisição e, nos pontos onde ações relevantes ocorrem 
(como alterações de status ou envio de notificações), o método registerAudit do AuditService é chamado.
Ao chamar registerAudit, os seguintes parâmetros são fornecidos:
description: Detalhes sobre a ação realizada.
action: Descrição da ação (por exemplo, "Alteração de Status", "Envio de Notificação").
userId: Identificador do usuário que realizou a ação.
Registro da Auditoria:

### Dentro do método registerAudit:

Um novo objeto Audit é criado a partir do RegisterAuditDTO usando o mapeador IAuditMapper.
O endereço IP é recuperado do ClientIpHolder.getClientIp() e definido no objeto Audit.
O timestamp atual é capturado e definido no objeto Audit.
O status da ação é determinado com base na lógica de negócio e definido no objeto Audit.
O objeto Audit é então salvo no repositório auditRepository.
Limpeza Pós-Requisição:

Após a conclusão do processamento da requisição, o interceptor limpa o ThreadLocal 
chamando ClientIpHolder.clear() para evitar vazamentos de memória e garantir 
que o contexto do IP não seja compartilhado entre diferentes requisições.

### Exemplo:

```java AuditService.java
private void sendRegisterAudit() {
    auditService.registerAudit(RegisterAuditDTO.builder()
        .description("Busca de contrato pelo número do contrato")
        .userId(userMongoService.getLoggedInUser().getId())
        .action("findContractByNumberContract")
        .status(RegisterAuditStatusEnum.SUCCESS)
        .build()
    );
}
```