# notificacao-service

Microsserviço que envia as notificações da oficina (WhatsApp ou SMS, via Twilio).
Não tem HTTP: só consome e publica mensagens no RabbitMQ.

```
API (backend)                         RabbitMQ                          notificacao-service
─────────────                         ────────                          ───────────────────
registra notificação "pendente"
RabbitEnviadorDeMensagem ──► notificacoes.exchange ──► notificacoes.envio ──► NotificacaoEnvioListener
                              (routing notificacao.envio)                       │
                                                                                ▼
                                                                      EnviarNotificacaoUseCase
                                                                      telefone → E.164 (+55...)
                                                                      Twilio (ou log, simulado)
                                                                                │
NotificacaoResultadoListener ◄── notificacoes.resultado ◄── notificacoes.exchange ◄┘
atualiza status: enviada / erro      (routing notificacao.resultado)

Falha temporária (Twilio fora, timeout, 429/5xx): 3 tentativas (2s, 4s, 8s).
Esgotou: a API recebe "erro" e a mensagem vai para notificacoes.envio.dlq.
```

## Contrato das mensagens (JSON)

| Fila | Campos |
|---|---|
| `notificacoes.envio` | `notificacaoId` (Long), `telefone` (só dígitos), `mensagem`, `contexto` |
| `notificacoes.resultado` | `notificacaoId` (Long), `enviada` (boolean), `erro` (texto ou null) |

Os nomes de filas, exchange e argumentos são **idênticos** nos dois `RabbitMQConfig`.
Se mudar um, mude o outro, ou o RabbitMQ recusa com `PRECONDITION_FAILED - inequivalent arg`.

## Como rodar

1. RabbitMQ rodando (um dos dois):
   ```bash
   brew install rabbitmq && brew services start rabbitmq
   rabbitmqctl add_user oficina SUA_SENHA
   rabbitmqctl set_permissions -p / oficina ".*" ".*" ".*"
   ```
   ou, com Docker, `RABBITMQ_PASSWORD=SUA_SENHA docker compose up -d` na pasta `Backend/`.
2. `cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties`
   e preencha com as mesmas credenciais do RabbitMQ usadas pela API.
3. `./mvnw spring-boot:run -Dspring-boot.run.profiles=local`

Com `twilio.enabled=false` (padrão), as mensagens só aparecem no log como `[SIMULADO]`
e voltam para a API como enviadas. É o modo para apresentar sem depender da Twilio.

## Twilio de graça (projeto acadêmico)

A conta trial não cobra nada e serve para testes. As limitações:

- **Só envia para números verificados** (até 5) ou que entraram no sandbox.
  Os clientes do banco têm telefones fictícios, então use `twilio.destino-teste`
  para mandar tudo para o seu celular.
- **WhatsApp pelo sandbox** (`+14155238886`): no celular que vai receber, mande
  `join <seu-código>` para **+1 415 523 8886** (o código aparece no Console em
  Messaging > Try it out > Send a WhatsApp message). A adesão vence em 3 dias.
- **Janela de 24h**: mensagem com texto livre (que é o caso da oficina) só é
  entregue até 24h depois da última mensagem que o celular mandou para o
  sandbox. Antes de apresentar, mande um "oi" para o sandbox.
- A Twilio avisa que mensagens do sandbox podem falhar para alguns países, Brasil incluído.
  Se não chegar, teste por SMS (`twilio.canal=sms`, com um número comprado usando
  o crédito trial e o Brasil liberado em Geo permissions).

Erros comuns aparecem traduzidos no log e na tela de notificações:

| Código | Causa | O que fazer |
|---|---|---|
| 20003 | SID/token errados | Copie de novo em Console > Account Info |
| 21608 | Trial enviando para número não verificado | Verifique o número ou use `destino-teste` |
| 21408 | País não liberado (SMS) | Messaging > Settings > Geo permissions > Brasil |
| 63007 | Remetente não é WhatsApp | `twilio.remetente=+14155238886` |
| 63015 | Celular não entrou no sandbox | Mande `join <código>` |
| 63016 | Fora da janela de 24h | Mande qualquer mensagem para o sandbox e reenvie |
