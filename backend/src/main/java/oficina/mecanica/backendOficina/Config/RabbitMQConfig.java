package oficina.mecanica.backendOficina.Config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "notificacoes.exchange";

    public static final String FILA_ENVIO = "notificacoes.envio";
    public static final String ROUTING_KEY_ENVIO = "notificacao.envio";

    public static final String FILA_RESULTADO = "notificacoes.resultado";
    public static final String ROUTING_KEY_RESULTADO = "notificacao.resultado";

    public static final String FILA_ENVIO_DLQ = "notificacoes.envio.dlq";
    public static final String ROUTING_KEY_ENVIO_DLQ = "notificacao.envio.dlq";

    @Bean
    public DirectExchange notificacoesExchange() {
        return new DirectExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue filaEnvio() {
        return QueueBuilder.durable(FILA_ENVIO)
                .withArgument("x-dead-letter-exchange", EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ROUTING_KEY_ENVIO_DLQ)
                .build();
    }

    @Bean
    public Queue filaEnvioDlq() {
        return QueueBuilder.durable(FILA_ENVIO_DLQ).build();
    }

    @Bean
    public Queue filaResultado() {
        return QueueBuilder.durable(FILA_RESULTADO).build();
    }

    @Bean
    public Binding bindingEnvio(Queue filaEnvio, DirectExchange notificacoesExchange) {
        return BindingBuilder.bind(filaEnvio).to(notificacoesExchange).with(ROUTING_KEY_ENVIO);
    }

    @Bean
    public Binding bindingEnvioDlq(Queue filaEnvioDlq, DirectExchange notificacoesExchange) {
        return BindingBuilder.bind(filaEnvioDlq).to(notificacoesExchange).with(ROUTING_KEY_ENVIO_DLQ);
    }

    @Bean
    public Binding bindingResultado(Queue filaResultado, DirectExchange notificacoesExchange) {
        return BindingBuilder.bind(filaResultado).to(notificacoesExchange).with(ROUTING_KEY_RESULTADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter conversor = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper mapeadorDeTipos = new DefaultJackson2JavaTypeMapper();
        mapeadorDeTipos.setTypePrecedence(DefaultJackson2JavaTypeMapper.TypePrecedence.INFERRED);
        conversor.setJavaTypeMapper(mapeadorDeTipos);
        return conversor;
    }
}