package es.uma.informatica.sii.jpa.trazabilidad;

import java.io.Closeable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

/**
 * Esta clase servirá para centralizar todas las operaciones relacionadas con la 
 * gestión de los datos relativos a los productos y sus lotes. 
 * Recuerda que las operaciones que modifiquen la base de datos deben ejecutarse dentro
 * de una transacción.
 * @author francis
 *
 */
public class AccesoDatos implements Closeable {
	
	private EntityManagerFactory emf;
	private EntityManager em;
	
	/**
	 * Constructor por defecto. Crea un contexto de persistencia.
	 */
	public AccesoDatos() {
		emf = Persistence.createEntityManagerFactory("jpa-trazabilidad");
		em = emf.createEntityManager();
	}
	
	/**
	 * Cierra el contexto de persistencia.
	 */
	@Override
	public void close() {
		em.close();
		emf.close();
	}
	
	/**
	 * Devuelve la lista de todos los productos que hay en la base de datos.
	 * @return Lista de proudctos
	 */
	
	public List<Producto> getListaProductos() {
		EntityTransaction tx=em.getTransaction();
		tx.begin();
		TypedQuery<Producto> query = em.createQuery("SELECT p from Producto p", Producto.class);
		List<Producto> listaProductos = query.getResultList();
		tx.commit();
		return listaProductos;
	}
	
	
	/**
	 * Elimina un producto y todos sus lotes asociados en la base de datos.
	 * El producto debe encontrarse en el contexto de persistencia.
	 * @param producto El producto a eliminar.
	 */
	public void eliminarProducto(Producto producto) {
		EntityTransaction tx=em.getTransaction();
		tx.begin();
		em.remove(producto);
		tx.commit();
		}
	
	/**
	 * Introduce un producto en la base de datos. 
	 * Este producto debe tener uun a lista de lotes vacía y no puede
	 * existir en el contexto de persistencia.
	 * @param producto Producto a introducir en la base de datos.
	 */
	
	public void crearProducto(Producto producto) {
		EntityTransaction tx=em.getTransaction();
		List<Lote> listaLote = new ArrayList<>();
		producto.setLotes(listaLote);
		tx.begin();
		em.persist(producto);
		tx.commit();
	}
	
	/**
	 * Añade un nuevo lote a un producto concreto.
	 * @param producto Producto al que se le añade el lote.
	 * @param lote Lote que se debe añadir. Este lote debe ser de nueva creación.
	 */
	public void aniadirLoteAProducto(Producto producto, Lote lote) {
		EntityTransaction tx = em.getTransaction();
		List<Lote> listaLote = producto.getLotes();
		listaLote.add(lote);	
		producto.setLotes(listaLote);
		tx.begin();
		em.persist(producto);
		tx.commit();
	}
	
	/**
	 * Elimina un lote de un producto y de la base de datos.
	 * @param producto Producto al que hay que quitarle un lote.
	 * @param lote Lote que se debe eliminar del producto. Este lote debe
	 * encontrarse asociado al producto.
	 */
	public void eliminarLoteDeProducto(Producto producto, Lote lote) {
		EntityTransaction tx = em.getTransaction();
		List<Lote> listaLote = producto.getLotes();
		listaLote.remove(lote);
		producto.setLotes(listaLote);
		tx.begin();
		em.persist(producto);
		tx.commit();
	}
	
}
