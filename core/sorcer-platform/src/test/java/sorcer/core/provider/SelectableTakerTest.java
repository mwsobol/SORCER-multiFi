package sorcer.core.provider;

import net.jini.space.JavaSpace05;
import org.junit.Before;
import org.junit.Test;
import sorcer.co.operator;
import sorcer.core.exertion.ExertionEnvelop;
import sorcer.core.signature.ServiceSignature;
import sorcer.service.Signature;
import sorcer.service.Task;
import sorcer.util.OperatingSystemType;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

public class SelectableTakerTest {
    private ServiceSignature serviceSignature;
    private SpaceTaker.SpaceTakerData spaceTakerData;

    @Before
    public void setup() {
        /* Create Signature */
        serviceSignature = new ServiceSignature();
        Signature.Operation operation = new Signature.Operation();
        serviceSignature.setOperation(operation);

        /* setup SpaceTakerData */
        spaceTakerData = new SpaceTaker.SpaceTakerData();
        spaceTakerData.entry = new ExertionEnvelop();
        ServiceExerter serviceExerter = new ServiceExerter();
        serviceExerter.getDelegate().setProviderName("taker test");
        spaceTakerData.provider = serviceExerter;
    }

    @Test
    public void testSelectSpaceEntryWithOsType() throws Exception {
        SelectableTaker selectableTaker = new SelectableTaker();
        serviceSignature.getOperation().setMatchTokens(osToken(OperatingSystemType.get()));

        JavaSpace05 space =
                (JavaSpace05) Proxy.newProxyInstance(JavaSpace05.class.getClassLoader(),
                                                     new Class[] { JavaSpace05.class },
                                                     new FakeSpace(serviceSignature));

        ExertionEnvelop exertionEnvelop = selectableTaker.selectSpaceEntry(spaceTakerData, space);
        assertNotNull(exertionEnvelop);
    }

    @Test
    public void testSelectSpaceEntryWithOsTypeList() throws Exception {
        SelectableTaker selectableTaker = new SelectableTaker();
        serviceSignature.getOperation().setMatchTokens(tokenList(osToken(OperatingSystemType.get())));

        JavaSpace05 space =
                (JavaSpace05) Proxy.newProxyInstance(JavaSpace05.class.getClassLoader(),
                                                     new Class[] { JavaSpace05.class },
                                                     new FakeSpace(serviceSignature));

        ExertionEnvelop exertionEnvelop = selectableTaker.selectSpaceEntry(spaceTakerData, space);
        assertNotNull(exertionEnvelop);
    }

    @Test
    public void testSelectSpaceEntryWithMultiOsType() throws Exception {
        SelectableTaker selectableTaker = new SelectableTaker();
        serviceSignature.getOperation().setMatchTokens(osToken(OperatingSystemType.MACINTOSH,
                                                               OperatingSystemType.LINUX,
                                                               OperatingSystemType.WINDOWS));

        JavaSpace05 space =
                (JavaSpace05) Proxy.newProxyInstance(JavaSpace05.class.getClassLoader(),
                                                     new Class[] { JavaSpace05.class },
                                                     new FakeSpace(serviceSignature));

        ExertionEnvelop exertionEnvelop = selectableTaker.selectSpaceEntry(spaceTakerData, space);
        assertNotNull(exertionEnvelop);
    }

    @Test
    public void testSelectSpaceEntryWithMultiOsTypeList() throws Exception {
        SelectableTaker selectableTaker = new SelectableTaker();
        serviceSignature.getOperation().setMatchTokens(tokenList(osToken(OperatingSystemType.MACINTOSH,
                                                                         OperatingSystemType.LINUX,
                                                                         OperatingSystemType.WINDOWS)));

        JavaSpace05 space =
                (JavaSpace05) Proxy.newProxyInstance(JavaSpace05.class.getClassLoader(),
                                                     new Class[] { JavaSpace05.class },
                                                     new FakeSpace(serviceSignature));

        ExertionEnvelop exertionEnvelop = selectableTaker.selectSpaceEntry(spaceTakerData, space);
        assertNotNull(exertionEnvelop);
    }

    @Test
    public void testSelectSpaceEntryWithBadOsTypeList() throws Exception {
        SelectableTaker selectableTaker = new SelectableTaker();
        serviceSignature.getOperation().setMatchTokens(tokenList(osToken("No Match")));

        JavaSpace05 space =
                (JavaSpace05) Proxy.newProxyInstance(JavaSpace05.class.getClassLoader(),
                                                     new Class[] { JavaSpace05.class },
                                                     new FakeSpace(serviceSignature));

        ExertionEnvelop exertionEnvelop = selectableTaker.selectSpaceEntry(spaceTakerData, space);
        assertNull(exertionEnvelop);
    }

    @Test
    public void testSelectSpaceEntryWithAppsList() throws Exception {
        SelectableTaker selectableTaker = new SelectableTaker();
        serviceSignature.getOperation().setMatchTokens(tokenList(appsToken("Fake")));

        JavaSpace05 space =
                (JavaSpace05) Proxy.newProxyInstance(JavaSpace05.class.getClassLoader(),
                                                     new Class[] { JavaSpace05.class },
                                                     new FakeSpace(serviceSignature));
        spaceTakerData.appNames = new ArrayList<>();
        spaceTakerData.appNames.add("Fake");

        ExertionEnvelop exertionEnvelop = selectableTaker.selectSpaceEntry(spaceTakerData, space);
        assertNotNull(exertionEnvelop);
    }

    @Test
    public void testSelectSpaceEntryWithMultiAppsList() throws Exception {
        SelectableTaker selectableTaker = new SelectableTaker();
        serviceSignature.getOperation().setMatchTokens(tokenList(appsToken("Fake1")));

        JavaSpace05 space =
                (JavaSpace05) Proxy.newProxyInstance(JavaSpace05.class.getClassLoader(),
                                                     new Class[] { JavaSpace05.class },
                                                     new FakeSpace(serviceSignature));
        spaceTakerData.appNames = new ArrayList<>();
        spaceTakerData.appNames.add("Fake");
        spaceTakerData.appNames.add("Fake1");
        spaceTakerData.appNames.add("Fake2");

        ExertionEnvelop exertionEnvelop = selectableTaker.selectSpaceEntry(spaceTakerData, space);
        assertNotNull(exertionEnvelop);
    }

    @Test
    public void testSelectSpaceEntryWithBadAppsList() throws Exception {
        SelectableTaker selectableTaker = new SelectableTaker();
        serviceSignature.getOperation().setMatchTokens(tokenList(appsToken("Fake")));

        JavaSpace05 space =
                (JavaSpace05) Proxy.newProxyInstance(JavaSpace05.class.getClassLoader(),
                                                     new Class[] { JavaSpace05.class },
                                                     new FakeSpace(serviceSignature));
        spaceTakerData.appNames = new ArrayList<>();
        spaceTakerData.appNames.add("Fail");

        ExertionEnvelop exertionEnvelop = selectableTaker.selectSpaceEntry(spaceTakerData, space);
        assertNull(exertionEnvelop);
    }

    @Test
    public void testSelectSpaceEntryWithNoConfiguredAppsList() throws Exception {
        SelectableTaker selectableTaker = new SelectableTaker();
        serviceSignature.getOperation().setMatchTokens(tokenList(appsToken("Fake")));

        JavaSpace05 space =
                (JavaSpace05) Proxy.newProxyInstance(JavaSpace05.class.getClassLoader(),
                                                     new Class[] { JavaSpace05.class },
                                                     new FakeSpace(serviceSignature));

        ExertionEnvelop exertionEnvelop = selectableTaker.selectSpaceEntry(spaceTakerData, space);
        assertNull(exertionEnvelop);
    }

    private operator.Tokens tokenList(operator.Tokens... tokens) {
        operator.Tokens tokenList = new operator.Tokens();
        tokenList.setType("LIST");
        Collections.addAll(tokenList, tokens);
        return tokenList;
    }

    private operator.Tokens appsToken(String... apps) {
        operator.Tokens appsToken = new operator.Tokens();
        appsToken.setType("APP");
        Collections.addAll(appsToken, apps);
        return appsToken;
    }

    private operator.Tokens osToken(String... osTypes) {
        operator.Tokens osToken = new operator.Tokens();
        osToken.setType("OS");
        Collections.addAll(osToken, osTypes);
        return osToken;
    }

    private static class FakeSpace implements InvocationHandler {
        ServiceSignature serviceSignature;

        public FakeSpace(ServiceSignature serviceSignature) {
            this.serviceSignature = serviceSignature;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            if (method.getName().equals("read")) {
                ExertionEnvelop envelop = new ExertionEnvelop();
                envelop.exertion = new Task(serviceSignature);
                return envelop;
            }
            return null;
        }
    }

}