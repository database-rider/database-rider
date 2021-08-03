package com.github.database.rider.core;

import com.github.database.rider.core.api.configuration.DBUnit;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.core.model.Order;
import com.github.database.rider.core.model.User;
import com.github.database.rider.core.util.EntityManagerProvider;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

import static com.github.database.rider.core.util.EntityManagerProvider.em;
import static com.github.database.rider.core.util.EntityManagerProvider.tx;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(JUnit4.class)
@DBUnit(escapePattern = "`?`", qualifiedTableNames = true)
public class EscapePatternIt {


    EntityManagerProvider emProvider = EntityManagerProvider.instance("escape-pattern");

    @Rule
    public TestRule theRule = RuleChain.outerRule(emProvider).
            around(DBUnitRule.instance(emProvider.connection()));

    @BeforeClass
    public static void init(){
        Order order = new Order();
        order.setName("@dbrider");
        tx("escape-pattern").begin();
        em("escape-pattern").persist(order);
        tx("escape-pattern").commit();
        List<Order> orders = em("escape-pattern").createQuery("select o from Order o").getResultList();
        assertThat(orders).isNotNull().hasSize(1);
    }

    /**
     * issue#136
     */
    @Test
    @DataSet(cleanBefore = true)
    public void shouldCleanDBUsingEscapePattern() {
        List<Order> orders = em("escape-pattern").createQuery("select o from Order o").getResultList();
        if(orders != null && !orders.isEmpty()){
            fail("Orders should be empty");
        }
    }

}
