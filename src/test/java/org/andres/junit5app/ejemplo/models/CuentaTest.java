package org.andres.junit5app.ejemplo.models;

import org.andres.junit5app.ejemplo.exceptions.DineroInsuficienteException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
//Assumptions lo utilizamos por ejemplo: si queremos desabilitar una prueba si la supocicion de que un servicio levantado falla.
//es decir, supongo que un servicio este levantado para la prueba, sino, no intento la misma.

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)//Per_method por defecto
//no es buena practica cambiar el ciclo de vida de test
class CuentaTest {
    static List<String> montoList() {
        return Arrays.asList("100","200","500","700","1000","5000");
    }
    Cuenta cuenta;
    private TestInfo testInfo; //para que lo pueda utilizar en casa test
    private  TestReporter testReporter; //para que lo pueda utilizar en casa test

    @BeforeEach
    void initMetodoTest(TestInfo testInfo, TestReporter testReporter){//inyecto mas dependencias para mostras mas informacion
        //System.out.println("Ejecutando: "+testInfo.getDisplayName() + " " +testInfo.getTestMethod().orElse(null).getName()+" con las etiquetas " + testInfo.getTags());
        testReporter.publishEntry("Ejecutando: "+testInfo.getDisplayName() + " " +testInfo.getTestMethod().orElse(null).getName()
                +" con las etiquetas " + testInfo.getTags());
        this.testInfo=testInfo;
        this.testReporter=testReporter;
        this.cuenta = new Cuenta("Andres", new BigDecimal("1000.3214"));
        System.out.println("Iniciando el metodo");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Finalizando el metodo");
    }

    @BeforeAll
    static void beforeAll() {
        System.out.println("Inicializando el test");
    }

    @AfterAll
    static void afterAll() {
        System.out.println("finalizando el test");
    }

    @Tag("cuenta")
    @Nested//podemos anidar clases para organizar las pruebas
    @DisplayName("Probando atributos de la Cuenta Corriente")
    class CuentaNombreYSaldo{
        @Test
        @DisplayName("Probando nombre!")
        void testNombreCuenta() {
            System.out.println(testInfo.getTags());
            if(testInfo.getTags().contains("cuenta")){
                System.out.println("Tiene etiqueta 'cuenta'");
            }
            //cuenta.setPersona("Andres");
            String esperado = "Andres";
            String real = cuenta.getPersona();
            assertEquals(esperado, real, () -> "El nombre de la cuenta no es el que se esperaba");
            //lo hago en una exprecion lamda para que no consuma recursos si es que no falla
        }

        @Test
        @DisplayName("Prebo que las cuenta no tenga saldo negativo")
        void testSaldoCuenta() {
            assertEquals(1000.3214, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test

        @DisplayName("Testeo referencias, que sean iguales")
        void testReferenciaDeCuenta() {
            Cuenta cuenta2 = new Cuenta("David", new BigDecimal("1254.3214"));
            assertNotEquals(cuenta2,cuenta);
            //assertEquals(cuenta2, cuenta);

        }
    }

    @Nested
    class CuantasOperacionesBanco{
        Cuenta cuenta;

        @BeforeEach
        void initMetodoTest(){
            this.cuenta = new Cuenta("Andres", new BigDecimal("1000.3214"));
            System.out.println("Iniciando el metodo");
        }

        @AfterEach
        void tearDown() {
            System.out.println("Finalizando el metodo");
        }

        @Test
        void testDebitoCuenta() {
            this.cuenta.debito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.3214", cuenta.getSaldo().toPlainString());
        }

        @Test
        void testCreditoCuenta() {
            this.cuenta.credito(new BigDecimal(100));
            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.3214", cuenta.getSaldo().toPlainString());
        }

        @Tag("error")
        @Tag("banco")
        @Test
        void testDineroInsuficienteException() {
            Exception exception = assertThrows(DineroInsuficienteException.class, () -> {
                this.cuenta.debito(new BigDecimal("2000"));
            });
            String actual = exception.getMessage();
            String esperado = "Dinero Insuficiente";
            assertEquals(esperado, actual);
        }

        @Test
        void testTransferirDineroCuentas() {
            Cuenta cuenta1 = new Cuenta("David", new BigDecimal("1000.366232"));
            Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1000.21321"));
            Banco banco = new Banco(new ArrayList<Cuenta>(), "Banco del Estado");
            banco.transferir(cuenta2, cuenta1, new BigDecimal("500"));
            assertEquals("500.21321", cuenta2.getSaldo().toPlainString());
            assertEquals("1500.366232", cuenta1.getSaldo().toPlainString());
        }

        @Test
        //@Disabled //no ejecuta el metodo, lo salta
        @DisplayName("Probando relacion entre cuentas y banco con AssertAll.")
        void testRelacionBancoCuentas() {
            //fail();//metodo estatico de Assertions para hacer fallar el metodo - fuerza el error
            Cuenta cuenta1 = new Cuenta("David", new BigDecimal("1000.366232"));
            Cuenta cuenta2 = new Cuenta("Andres", new BigDecimal("1000.21321"));
            Banco banco = new Banco(new ArrayList<Cuenta>(), "Banco del Estado");
            banco.addCuenta(cuenta1);
            banco.addCuenta(cuenta2);
            assertAll(
                    () -> {
                        assertEquals(2, banco.getCuentas().size());
                    },
                    () -> {
                        assertEquals("Banco del Estado", cuenta1.getBanco().getNombre());
                    },
                    () -> {
                        assertEquals("David", banco.getCuentas().stream().filter(c -> c.getPersona().equals("David")).findFirst().get().getPersona());
                    },
                    () -> {
                        assertTrue(banco.getCuentas().stream().anyMatch(c -> c.getPersona().equals("Andres")));
                    });
        }
    }

    @Nested
    class SistemaOperativoTest{
        @Test
        @EnabledOnOs(OS.WINDOWS)
        void testSoloWindows() {
        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC})
        void testSoloLinuxMac() {
        }

        @Test
        @DisabledOnOs(OS.WINDOWS)
        void testNoWindows() {
        }
    }

    @Nested
    class JavaVersionTest{
        @Test
        @EnabledOnJre(JRE.JAVA_8)
        void testSoloJdk8() {
        }
    }

    @Nested
    class SystemPropertiestest{
        @Test //uso este test para conocer las propiedades
        void imprimirSystemProperties() {
            Properties properties = System.getProperties();
            properties.forEach((k, v) -> System.out.println(k + ":" + v));
        }

        @Test
        @EnabledIfSystemProperty(named = "java.version", matches = "11.0.10")
        void testJavaVersion() {
        }
    }

    @Nested
    class EnvironmentVarabletest{
        @Test //uso este test para conocer las variables de entorno
        void imprimirVariablesAmbiente() {
            Map<String, String> getenv = System.getenv();
            getenv.forEach((k, v) -> System.out.println(k + " = " + v));
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = "/usr/lib/jvm/java-11-oracle")
        void testJavaHome() {
        }

        @Test
        @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
            //solo para desarrollo, se debe agregar en la configuracion del run/debug, agregar ENVIRONMENT=dev en Environment Variables
        void testEnv() {
        }
    }

    @Test
    @DisplayName("Pruebo que las cuenta no tenga saldo negativo, solo si estamos en desarrollo")
    void testSaldoCuentaDev() {
        boolean isDev = "dev".equals(System.getProperty("ENV"));
        assumeTrue(isDev);
        assertEquals(1000.3214, cuenta.getSaldo().doubleValue());
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    @DisplayName("Solo si estamos en desarrollo")
    void testSaldoCuentaDev2() {
        boolean isDev = "dev".equals(System.getProperty("ENV"));
        assumingThat(isDev, ()->{//ejecuta segun la condicion, solo el bloque de codigo de la expresion lamda
            assertEquals(1000.3214, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            System.out.println("Pase por aca: es dev");
        });
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        System.out.println("Pase por aca: es prod");
    }

    //para repetir test
    @DisplayName("Debito Cuenta Repetir")
    @RepeatedTest(value = 5,name = "{displayName} - Repetición número: {currentRepetition} de {totalRepetitions}")
    void testDebitoCuentaRepetir(RepetitionInfo info) {//inyeccion de dependencias, nos pasa una instancia de JUnit
        if(info.getCurrentRepetition() == 3){
            System.out.println("Esta fue la 3");
        }
        this.cuenta.debito(new BigDecimal(100));
        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.3214", cuenta.getSaldo().toPlainString());
    }

    @Tag("param")
    @ParameterizedTest(name = "número {index} - ejecutando con el valor {argumentsWithNames}")
    @MethodSource("montoList")
    void testDebitoCuentaMethodSource(String monto) {
        this.cuenta.debito(new BigDecimal(monto));
        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Tag("param")
    @Nested
    class PruebasParametrizadastest {
        Cuenta cuenta;

        @BeforeEach
        void initMetodoTest() {
            this.cuenta = new Cuenta("Andres", new BigDecimal("1000.3214"));
            System.out.println("Iniciando el metodo");
        }

        @AfterEach
        void tearDown() {
            System.out.println("Finalizando el metodo");
        }

        //para trabajar con parametros en los test, debe ir acompañado de algun source
        //se repetira la prueba con cada elemento del array
        @ParameterizedTest(name = "número {index} - ejecutando con el valor {argumentsWithNames}")
        @ValueSource(strings = {"100", "200", "500", "700", "1000", "5000"})
        void testDebitoCuentaValueSource(String monto) {
            this.cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);

        }

        @ParameterizedTest(name = "número {index} - ejecutando con el valor {argumentsWithNames}")
        @CsvSource({"1,100", "2,200", "3,500", "4,700", "5,1000", "6,5000"})
        void testDebitoCuentaCsvSource(String index, String monto) {
            System.out.println(index + " -> " + monto);
            this.cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "número {index} - ejecutando con el valor {argumentsWithNames}")
        @CsvFileSource(resources = "data.csv")
        void testDebitoCuentaCsvFileSource(String monto) {
            this.cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }

        @ParameterizedTest(name = "número {index} - ejecutando con el valor {argumentsWithNames}")
        @CsvSource({"200,100", "250,200", "699,500", "400,700", "577,1000", "5000,5000"})
        void testDebitoCuentaCsvSource2(String saldo, String monto) {
            System.out.println(saldo + " -> " + monto);
            this.cuenta.setSaldo(new BigDecimal(saldo));
            this.cuenta.debito(new BigDecimal(monto));
            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
        }
    }

    //testeando por timeout
    @Tag("timeOut")
    @Nested
    class timeOutTest{
        @Test
        @Timeout(1)
        void pruebaTimeOut() throws InterruptedException {
            TimeUnit.SECONDS.sleep(1);
        }

        @Test
        @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
        void pruebaTimeOut2() throws InterruptedException {
            TimeUnit.SECONDS.sleep(1);
        }

        @Test
        void pruebaTimeOut3() {
            assertTimeout(Duration.ofSeconds(5), ()->{
                TimeUnit.MILLISECONDS.sleep(5000);
            });
        }
    }
}