package br.gov.servicos.editor.frontend;

import lombok.experimental.FieldDefaults;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.util.Iterator;
import java.util.UUID;

import static br.gov.servicos.editor.frontend.TicketFilter.TICKET_KEY;
import static java.util.UUID.*;
import static lombok.AccessLevel.PRIVATE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@FieldDefaults(level = PRIVATE)
public class TicketFilterTest {

    public static final UUID TICKET = randomUUID();

    TicketFilter filter;

    @Mock
    Iterator<UUID> tickets;

    @Before
    public void setUp() throws Exception {
        filter = new TicketFilter(tickets);
    }

    private static MockFilterChain chain() {
        return new MockFilterChain();
    }

    private static MockHttpServletResponse response() {
        return new MockHttpServletResponse();
    }

    private static MockHttpServletRequest request() {
        return new MockHttpServletRequest();
    }

    @Test
    public void adicionaTicketAoRequest() throws Exception {
        when(tickets.next()).thenReturn(TICKET);

        ServletRequest request = request();
        filter.doFilter(request, response(), chain());

        assertThat(request.getAttribute(TICKET_KEY), is(TICKET.toString()));
        verify(tickets, atMost(1)).next();
    }

    @Test
    public void modificaTicketParaCadaNovoRequest() throws Exception {
        UUID ticket1 = randomUUID();
        UUID ticket2 = randomUUID();

        when(tickets.next())
                .thenReturn(ticket1)
                .thenReturn(ticket2);

        filter.doFilter(request(), response(), chain());
        filter.doFilter(request(), response(), chain());

        assertThat(ticket1, is(not(ticket2)));

        verify(tickets, atMost(2)).next();
    }

    @Test
    public void sempreContinuaExecucaoDosFiltros() throws Exception {
        when(tickets.next()).thenReturn(TICKET);

        MockFilterChain chain = chain();
        ServletRequest request = request();
        ServletResponse response = response();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest(), is(request));
        assertThat(chain.getResponse(), is(response));
    }
}
