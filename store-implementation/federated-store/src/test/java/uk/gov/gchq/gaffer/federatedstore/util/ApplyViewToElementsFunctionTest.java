package uk.gov.gchq.gaffer.federatedstore.util;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

import uk.gov.gchq.gaffer.accumulostore.AccumuloStore;
import uk.gov.gchq.gaffer.accumulostore.SingleUseMiniAccumuloStore;
import uk.gov.gchq.gaffer.accumulostore.retriever.impl.AccumuloAllElementsRetriever;
import uk.gov.gchq.gaffer.data.element.Element;
import uk.gov.gchq.gaffer.data.elementdefinition.view.View;
import uk.gov.gchq.gaffer.graph.Graph;
import uk.gov.gchq.gaffer.graph.GraphConfig;
import uk.gov.gchq.gaffer.operation.impl.add.AddElements;
import uk.gov.gchq.gaffer.operation.impl.get.GetAllElements;
import uk.gov.gchq.gaffer.user.StoreUser;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.gchq.gaffer.federatedstore.FederatedStoreTestUtil.ACCUMULO_STORE_SINGLE_USE_PROPERTIES;
import static uk.gov.gchq.gaffer.federatedstore.FederatedStoreTestUtil.GRAPH_ID_ACCUMULO;
import static uk.gov.gchq.gaffer.federatedstore.FederatedStoreTestUtil.SCHEMA_EDGE_BASIC_JSON;
import static uk.gov.gchq.gaffer.federatedstore.FederatedStoreTestUtil.contextBlankUser;
import static uk.gov.gchq.gaffer.federatedstore.FederatedStoreTestUtil.edgeBasic;
import static uk.gov.gchq.gaffer.federatedstore.FederatedStoreTestUtil.loadAccumuloStoreProperties;
import static uk.gov.gchq.gaffer.federatedstore.FederatedStoreTestUtil.loadSchemaFromJson;

class ApplyViewToElementsFunctionTest {


    @Test
    public void shouldIterateWithAccumuloElementRetriever() throws Exception {
        final AccumuloStore singleUseAccumuloStore = new SingleUseMiniAccumuloStore();
        singleUseAccumuloStore.initialise(GRAPH_ID_ACCUMULO, loadSchemaFromJson(SCHEMA_EDGE_BASIC_JSON), loadAccumuloStoreProperties(ACCUMULO_STORE_SINGLE_USE_PROPERTIES));
        singleUseAccumuloStore.execute(new AddElements.Builder().input(edgeBasic()).build(), contextBlankUser());

        final Graph graph = new Graph.Builder()
                .addSchema(loadSchemaFromJson(SCHEMA_EDGE_BASIC_JSON))
                .addStoreProperties(loadAccumuloStoreProperties(ACCUMULO_STORE_SINGLE_USE_PROPERTIES))
                .config(new GraphConfig(GRAPH_ID_ACCUMULO))
                .build();

        graph.execute(new AddElements.Builder().input(edgeBasic()).build(), contextBlankUser());

        final Iterable<? extends Element> getAllElements = graph.execute(new GetAllElements.Builder().build(), contextBlankUser());

        final AccumuloAllElementsRetriever elements = new AccumuloAllElementsRetriever(singleUseAccumuloStore, new GetAllElements.Builder().view(new View()).build(), StoreUser.blankUser());

        assertThat(getAllElements)
                .isExactlyInstanceOf(AccumuloAllElementsRetriever.class)
                .asInstanceOf(InstanceOfAssertFactories.iterable(Element.class))
                .containsExactly(edgeBasic());

        assertThat(elements)
                .isExactlyInstanceOf(AccumuloAllElementsRetriever.class)
                .asInstanceOf(InstanceOfAssertFactories.iterable(Element.class))
                .containsExactly(edgeBasic());
    }

}