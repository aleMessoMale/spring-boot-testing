package guru.springframework.brewery.events;

import com.github.jenspiegsa.wiremockextension.Managed;
import com.github.jenspiegsa.wiremockextension.WireMockExtension;
import com.github.tomakehurst.wiremock.WireMockServer;
import guru.springframework.brewery.domain.BeerOrder;
import guru.springframework.brewery.domain.OrderStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static com.github.jenspiegsa.wiremockextension.ManagedWireMockServer.with;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

@ExtendWith(WireMockExtension.class)
class BeerOrderStatusChangeEventListenerTest {

    /*
        sarà managed da wiremock extensions e sarà esposto su una porta random
     */
    @Managed
    WireMockServer wireMockServer = with(wireMockConfig().dynamicPort());

    BeerOrderStatusChangeEventListener listener;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        listener = new BeerOrderStatusChangeEventListener(restTemplateBuilder);

    }

    @Test
    void listen() {

        /*
            necessario stubbare la richiesta di cui fornirà una risposta
         */
        wireMockServer.stubFor(post("/update").willReturn(ok()));

        BeerOrder beerOrder = BeerOrder.builder()
                    .orderStatus(OrderStatusEnum.READY)
                /*
                    abbiamo la possibilità logicamente di recuperare la porta su cui è esposto il mock
                 */
                .orderStatusCallbackUrl("http://localhost:" + wireMockServer.port() + "/update")
                .createdDate(Timestamp.valueOf(LocalDateTime.now()))
                .build();

        BeerOrderStatusChangeEvent event = new BeerOrderStatusChangeEvent(beerOrder, OrderStatusEnum.NEW);

        listener.listen(event);

        verify(1, postRequestedFor(urlEqualTo("/update")));

    }
}