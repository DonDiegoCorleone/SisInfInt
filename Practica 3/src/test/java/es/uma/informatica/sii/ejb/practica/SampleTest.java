package es.uma.informatica.sii.ejb.practica;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import es.uma.informatica.sii.ejb.practica.ejb.GestionLotes;
import es.uma.informatica.sii.ejb.practica.ejb.GestionProductos;
import es.uma.informatica.sii.ejb.practica.ejb.exceptions.IngredientesIncorrectosException;
import es.uma.informatica.sii.ejb.practica.ejb.exceptions.LoteExistenteException;
import es.uma.informatica.sii.ejb.practica.ejb.exceptions.LoteNoEncontradoException;
import es.uma.informatica.sii.ejb.practica.ejb.exceptions.ProductoNoEncontradoException;
import es.uma.informatica.sii.ejb.practica.ejb.exceptions.TrazabilidadException;
import es.uma.informatica.sii.ejb.practica.entidades.Ingrediente;
import es.uma.informatica.sii.ejb.practica.entidades.Lote;
import es.uma.informatica.sii.ejb.practica.entidades.Producto;

public class SampleTest {
	
	private static final Logger LOG = Logger.getLogger(SampleTest.class.getCanonicalName());

	private static final String PRODUCTOS_EJB = "java:global/classes/ProductosEJB";
	private static final String GLASSFISH_CONFIGI_FILE_PROPERTY = "org.glassfish.ejb.embedded.glassfish.configuration.file";
	private static final String CONFIG_FILE = "target/test-classes/META-INF/domain.xml";
	private static final String LOTES_EJB = "java:global/classes/LotesEJB";
	private static final String UNIDAD_PERSITENCIA_PRUEBAS = "TrazabilidadTest";
	
	private static EJBContainer ejbContainer;
	private static Context ctx;
	
	private GestionLotes gestionLotes;
	private GestionProductos gestionProductos;
	
	@BeforeClass
	public static void setUpClass() {
		Properties properties = new Properties();
		properties.setProperty(GLASSFISH_CONFIGI_FILE_PROPERTY, CONFIG_FILE);
		ejbContainer = EJBContainer.createEJBContainer(properties);
		ctx = ejbContainer.getContext();
	}
	
	@Before
	public void setup() throws NamingException  {
		gestionLotes = (GestionLotes) ctx.lookup(LOTES_EJB);
		gestionProductos = (GestionProductos) ctx.lookup(PRODUCTOS_EJB);
		BaseDatos.inicializaBaseDatos(UNIDAD_PERSITENCIA_PRUEBAS);
	}

	@Test
	public void testInsertarLote() {
		
		final String productoSalchicha = "Salchicha";
		
		try {
			
			
			Lote lote = new Lote ("ST1", null, BigDecimal.TEN, Date.valueOf("2021-04-11"));
			lote.setLoteIngredientes(new HashMap<Ingrediente, String>());
			
			Producto salchicha = gestionProductos.obtenerProductoEIngredientes(productoSalchicha);
			salchicha.getIngredientes().forEach(ingrediente->{
				lote.getLoteIngredientes().put(ingrediente, "");
			});
			
			try {
				gestionLotes.insertarLote(productoSalchicha, lote);
			} catch (ProductoNoEncontradoException|IngredientesIncorrectosException|LoteExistenteException e) {
				fail("Lanz?? excepci??n al insertar");
			}
		} catch (TrazabilidadException e) {
			throw new RuntimeException(e);
		}
				
		try {
			List<Lote> lotes = gestionLotes.obtenerLotesDeProducto(productoSalchicha);
			assertEquals(1, lotes.size());
			assertEquals(4,lotes.get(0).getLoteIngredientes().size());
			assertEquals("ST1", lotes.get(0).getCodigo());
			assertTrue(BigDecimal.valueOf(10L).compareTo(lotes.get(0).getCantidad())==0);
			assertEquals(Date.valueOf("2021-04-11"), lotes.get(0).getFechaFabricacion());
		} catch (TrazabilidadException e) {
			fail("No deber??a lanzar excepci??n");
		}
	}
	
	@Test
	public void testInsertarLoteProductoNoEncontrado() {
		try {
			final String productoSalchicha = "Salchicha";

			Lote lote = new Lote ("ST1", null, BigDecimal.TEN, Date.valueOf("2021-04-11"));
			lote.setLoteIngredientes(new HashMap<Ingrediente, String>());
			
			Producto salchicha = gestionProductos.obtenerProductoEIngredientes(productoSalchicha);
			salchicha.getIngredientes().forEach(ingrediente->{
				lote.getLoteIngredientes().put(ingrediente, "");
			});
			
			try {
				gestionLotes.insertarLote("Salchich??n", lote);
				fail("Debe lanzar excepci??n");
			} catch (ProductoNoEncontradoException e) {
				// OK
			} catch (TrazabilidadException e) {
				fail("Debe lanzar excepci??n de producto no encontrado"); 
			}
			
		} catch (TrazabilidadException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testInsertarLoteIngredientesIncorrectos() {

		try {
			final String productoSalchicha = "Salchicha";

			Lote lote = new Lote ("ST1", null, BigDecimal.TEN, Date.valueOf("2021-04-11"));
			lote.setLoteIngredientes(new HashMap<Ingrediente, String>());

			Producto salchicha = gestionProductos.obtenerProductoEIngredientes(productoSalchicha);
			lote.getLoteIngredientes().put(
					salchicha.getIngredientes().stream()
					.findAny().get(), 
					"");

			LOG.info("Test: "+lote.getLoteIngredientes().keySet());
			
			try {
				gestionLotes.insertarLote(productoSalchicha, lote);
				fail("Debe lanzar excepci??n");
			} catch (IngredientesIncorrectosException e) {
				// OK
			} catch (TrazabilidadException e) {
				fail("Debe lanzar excepci??n de ingredientes incorrectos");
			} 

		} catch (TrazabilidadException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testInsertarLoteExistente() {

		try {
			final String nombreProducto = "Chorizo";

			Lote lote = new Lote ("LT1", null, BigDecimal.TEN, Date.valueOf("2021-04-11"));
			lote.setLoteIngredientes(new HashMap<Ingrediente, String>());

			Producto chorizo = gestionProductos.obtenerProductoEIngredientes(nombreProducto);
			lote.getLoteIngredientes().put(
					chorizo.getIngredientes().stream()
					.findAny().get(), 
					"");

			try {
				gestionLotes.insertarLote(nombreProducto, lote);
				fail("Debe lanzar excepci??n de lote existente");
			} catch (LoteExistenteException e) {
				// OK
			} catch (TrazabilidadException e) {
				fail("Debe lanzar excepci??n de lote existente");
			} 

		} catch (TrazabilidadException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testObtenerLotes() {
		try {
			List<Lote> lotes = gestionLotes.obtenerLotesDeProducto("Chorizo");
			assertEquals(2, lotes.size());
		} catch (TrazabilidadException e) {
			fail("No deber??a lanzar excepci??n");
		}
	}
	
	@Test
	public void testObtenerLotesProductoNoEncontrado() {
		try {
			List<Lote> lotes = gestionLotes.obtenerLotesDeProducto("Arroz");
			fail("Deber??a lanzar excepci??n de producto no encontrado");
		} catch (ProductoNoEncontradoException e) {
			// OK
		} catch (TrazabilidadException e) {
			fail("Deber??a lanzar excepci??n de producto no encontrado");
		}
	}
	
	@Test
	public void testActualizarLote() {
		
		final String nombreProducto = "Chorizo";
		
		final long nuevaCantidad = 1234L;
		final String nuevaFecha = "2020-01-01";
		final String nuevoLoteCarne = "ABCD";
		final String nombreIngrediente = "Carne picada";
		String codigoLote1=null;
		
		try {
			
			List<Lote> lotes = gestionLotes.obtenerLotesDeProducto(nombreProducto);
			Lote lote1 = lotes.get(0);
			
			codigoLote1 = lote1.getCodigo(); 
			
			lote1.setCantidad(BigDecimal.valueOf(nuevaCantidad));
			lote1.setFechaFabricacion(Date.valueOf(nuevaFecha));
			Ingrediente carne = lote1.getLoteIngredientes().keySet().stream()
				.filter(ingrediente->{return ingrediente.getNombre().equals(nombreIngrediente);})
				.findAny().get();
			
			lote1.getLoteIngredientes().put(carne, nuevoLoteCarne);
			
			gestionLotes.actualizarLote(nombreProducto, lote1);

		} catch (TrazabilidadException e) {
			fail("Lanz?? excepci??n al actualizar");
		}


		try {
			final String codigoLoteActualizado=codigoLote1;
			Lote loteActualizado = gestionLotes.obtenerLotesDeProducto(nombreProducto).stream()
					.filter(lote->{return lote.getCodigo().equals(codigoLoteActualizado);})
					.findAny().get();
			
			assertTrue(BigDecimal.valueOf(nuevaCantidad).compareTo(loteActualizado.getCantidad())==0);
			assertEquals(Date.valueOf(nuevaFecha), loteActualizado.getFechaFabricacion());
			Ingrediente carne = loteActualizado.getLoteIngredientes().keySet().stream()
					.filter(ingrediente->{return ingrediente.getNombre().equals(nombreIngrediente);})
					.findAny().get();
			
			assertEquals(nuevoLoteCarne, loteActualizado.getLoteIngredientes().get(carne));
		} catch (TrazabilidadException e) {
			fail("No deber??a lanzar excepci??n");
		}
	}
	
	@Test
	public void testActualizarLoteProductoNoEncontrado() {
		final String nombreProducto = "Chorizo";
		final String otroProducto = "Arroz";
		
		final long nuevaCantidad = 1234L;
		final String nuevaFecha = "2020-01-01";
		final String nuevoLoteCarne = "ABCD";
		final String nombreIngrediente = "Carne picada";
		
		try {
			
			List<Lote> lotes = gestionLotes.obtenerLotesDeProducto(nombreProducto);
			Lote lote1 = lotes.get(0);
			
			lote1.setCantidad(BigDecimal.valueOf(nuevaCantidad));
			lote1.setFechaFabricacion(Date.valueOf(nuevaFecha));
			Ingrediente carne = lote1.getLoteIngredientes().keySet().stream()
				.filter(ingrediente->{return ingrediente.getNombre().equals(nombreIngrediente);})
				.findAny().get();
			
			lote1.getLoteIngredientes().put(carne, nuevoLoteCarne);
			
			gestionLotes.actualizarLote(otroProducto, lote1);
			fail("Deber??a lanzar excepci??n de producto no encontrado");
		} catch (ProductoNoEncontradoException e) {
			// OK
		} catch (TrazabilidadException e) {
			fail("Deber??a lanzar excepci??n de producto no encontrado");
		}
	}
	
	@Test
	public void testActualizarLoteoNoEncontrado() {
		final String nombreProducto = "Chorizo";
		final String nuevoCodigoLote = "OtroCodigo";
		
		try {
			List<Lote> lotes = gestionLotes.obtenerLotesDeProducto(nombreProducto);
			Lote lote1 = lotes.get(0);
			lote1.setCodigo(nuevoCodigoLote);
			gestionLotes.actualizarLote(nombreProducto, lote1);
			fail("Deber??a lanzar excepci??n de lote no encontrado");
		} catch (LoteNoEncontradoException e) {
			// OK
		} catch (TrazabilidadException e) {
			fail("Deber??a lanzar excepci??n de lote no encontrado");
		}
	}
	
	@Test
	public void testActualizarLoteIngredientesIncorrectos() {
		final String nombreProducto = "Chorizo";
		final String nombreIngrediente = "Carne picada";
		
		try {
			
			List<Lote> lotes = gestionLotes.obtenerLotesDeProducto(nombreProducto);
			Lote lote1 = lotes.get(0);
			
			Ingrediente carne = lote1.getLoteIngredientes().keySet().stream()
				.filter(ingrediente->{return ingrediente.getNombre().equals(nombreIngrediente);})
				.findAny().get();
			
			lote1.getLoteIngredientes().remove(carne);
			
			gestionLotes.actualizarLote(nombreProducto, lote1);
			fail("Deber??a lanzar excepci??n de ingredientes incorrectos");
		} catch (IngredientesIncorrectosException e) {
			// OK
		} catch (TrazabilidadException e) {
			fail("Deber??a lanzar excepci??n de ingredientes incorrectos");
		} 
	}
	
	@Test
	public void testEliminarLote() {
		try {
			final String nombreProducto = "Chorizo";
			List<Lote>  lotes = gestionLotes.obtenerLotesDeProducto(nombreProducto);
			
			Lote lote1 = lotes.get(0);
			
			gestionLotes.eliminarLote(nombreProducto, lote1);
			
			lotes = gestionLotes.obtenerLotesDeProducto(nombreProducto);
			assertEquals(1, lotes.size());
			
		} catch (TrazabilidadException e) {
			fail("No deber??a lanzarse excepci??n");
		}
	}
	
	@Test
	public void testEliminarLoteProductoNoEncontrado() {
		try {
			final String nombreProducto = "Chorizo";
			final String otroProducto = "Arroz";
			
			List<Lote>  lotes = gestionLotes.obtenerLotesDeProducto(nombreProducto);
			
			Lote lote1 = lotes.get(0);

			gestionLotes.eliminarLote(otroProducto, lote1);
			fail("Deber??a lanzar la excepci??n de producto no encontrado");
		} catch (ProductoNoEncontradoException e) {
			// OK
		} catch (TrazabilidadException e) {
			fail("Deber??a lanzar la excepci??n de producto no encontrado");
		}
	}
	
	@Test
	public void testEliminarLoteNoEncontrado() {
		try {
			final String nombreProducto = "Chorizo";
			
			List<Lote>  lotes = gestionLotes.obtenerLotesDeProducto(nombreProducto);
			Lote lote1 = lotes.get(0);
			
			lote1.setCodigo("Otro");

			gestionLotes.eliminarLote(nombreProducto, lote1);
			fail("Deber??a lanzar la excepci??n de lote no encontrado");
		} catch (LoteNoEncontradoException e) {
			// OK
		} catch (TrazabilidadException e) {
			fail("Deber??a lanzar la excepci??n de lote no encontrado");
		}
	}
	
	@Test
	public void testEliminarTodosLotes() {
		try {
			final String nombreProducto = "Chorizo";
			gestionLotes.eliminarTodosLotes(nombreProducto);
			
			List<Lote> lotes = gestionLotes.obtenerLotesDeProducto(nombreProducto);
			assertEquals(0, lotes.size());
			
		} catch (TrazabilidadException e) {
			fail("No deber??a lanzarse excepci??n");
		}
	}
	
	@Test
	public void testEliminarTodosLotesProductoNoEncontrado() {
		try {
			final String nombreProducto = "Arroz";
			gestionLotes.eliminarTodosLotes(nombreProducto);
			
			fail("Deber??a lanzar la excepci??n de producto no encontrado");
		} catch (ProductoNoEncontradoException e) {
			// OK
		} catch(TrazabilidadException e) {
			fail("Deber??a lanzar la excepci??n de producto no encontrado");
		}
	}
	
	@AfterClass
	public static void tearDownClass() {
		if (ejbContainer != null) {
			ejbContainer.close();
		}
	}

}
