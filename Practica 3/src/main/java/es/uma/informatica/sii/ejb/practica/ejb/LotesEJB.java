package es.uma.informatica.sii.ejb.practica.ejb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import es.uma.informatica.sii.ejb.practica.ejb.exceptions.IngredientesIncorrectosException;
import es.uma.informatica.sii.ejb.practica.ejb.exceptions.LoteExistenteException;
import es.uma.informatica.sii.ejb.practica.ejb.exceptions.LoteNoEncontradoException;
import es.uma.informatica.sii.ejb.practica.ejb.exceptions.ProductoNoEncontradoException;
import es.uma.informatica.sii.ejb.practica.entidades.Lote;
import es.uma.informatica.sii.ejb.practica.entidades.Producto;

/**
 * Session Bean implementation class Sample
 */
@Stateless
public class LotesEJB implements GestionLotes {
	
	private static final Logger LOG = Logger.getLogger(LotesEJB.class.getCanonicalName());
	
	@PersistenceContext(name="Trazabilidad")
	private EntityManager em;

	@Override
	public void insertarLote(String producto, Lote lote) throws ProductoNoEncontradoException, LoteExistenteException, IngredientesIncorrectosException {
		Producto productoEntity = em.find(Producto.class, producto);
		if (productoEntity == null) {
			throw new ProductoNoEncontradoException();
		}
		
		Lote loteExistente = em.find(Lote.class, new Lote.LoteId(lote.getCodigo(), producto));
		if (loteExistente != null) {
			throw new LoteExistenteException();
		}
		
		if (!productoEntity.getIngredientes().equals(lote.getLoteIngredientes().keySet())) {
			throw new IngredientesIncorrectosException();
		}
		
		lote.setProducto(productoEntity);
		em.persist(lote);
	}

	@Override
	public List<Lote> obtenerLotesDeProducto(String nombre) throws ProductoNoEncontradoException {
		Producto producto = em.find(Producto.class, nombre);
		if (producto == null) {
			throw new ProductoNoEncontradoException();
		}
		producto.getLotes().size();
		return new ArrayList<>(producto.getLotes());
	}

	@Override
	public void actualizarLote(String producto, Lote lote) throws ProductoNoEncontradoException, LoteNoEncontradoException,IngredientesIncorrectosException{
		Producto productoEntity = em.find(Producto.class, producto);
		if (productoEntity == null) {
			throw new ProductoNoEncontradoException();
		}
		Lote loteExistente = em.find(Lote.class, new Lote.LoteId(lote.getCodigo(), producto));
		if (loteExistente == null) {
			throw new  LoteNoEncontradoException();
		}
		if (!productoEntity.getIngredientes().equals(lote.getLoteIngredientes().keySet())) {
			throw new IngredientesIncorrectosException();
		}
		
		loteExistente.setFechaFabricacion(lote.getFechaFabricacion());
		loteExistente.setCantidad(lote.getCantidad());
		loteExistente.setLoteIngredientes(lote.getLoteIngredientes());
		em.persist(productoEntity);
		
	}

	@Override
	public void eliminarLote(String producto, Lote lote)
			throws ProductoNoEncontradoException, LoteNoEncontradoException {
		Producto productoEntity = em.find(Producto.class, producto);
		if (productoEntity == null) {
			throw new ProductoNoEncontradoException();
		}
		Lote loteExistente = em.find(Lote.class, new Lote.LoteId(lote.getCodigo(), producto));
		if (loteExistente == null) {
			throw new  LoteNoEncontradoException();
		}
		
		Set <Lote> lotes=productoEntity.getLotes();
		lotes.remove(lote);
		productoEntity.setLotes(lotes);
		em.persist(productoEntity);
	}

	@Override
	public void eliminarTodosLotes(String producto) throws ProductoNoEncontradoException {
		Producto productoEntity = em.find(Producto.class, producto);
		if (productoEntity == null) {
			throw new ProductoNoEncontradoException();
		}
		
		Set <Lote> lotes=null;
		productoEntity.setLotes(lotes);
		em.persist(productoEntity);
	}

    

}
