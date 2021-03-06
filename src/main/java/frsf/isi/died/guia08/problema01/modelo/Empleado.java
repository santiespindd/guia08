package frsf.isi.died.guia08.problema01.modelo;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import frsf.isi.died.guia08.problema01.excepciones.AsignacionIncorrectaException;
import frsf.isi.died.guia08.problema01.excepciones.TareaException;

public class Empleado {

	public enum Tipo {
		CONTRATADO, EFECTIVO
	};

	private Integer cuil;
	private String nombre;
	private Tipo tipo;
	private Double costoHora;
	private List<Tarea> tareasAsignadas;

	private Function<Tarea, Double> calculoPagoPorTarea;
	private Predicate<Tarea> puedeAsignarTarea;
	
	

	public Empleado(Integer cuil, String nombre, Tipo tipo, Double costoHora) {
		
		this.cuil = cuil;
		this.nombre = nombre;
		this.tipo = tipo;
		this.costoHora = costoHora;
		this.tareasAsignadas = new ArrayList<Tarea>();
		this.puedeAsignarTarea = ((a) -> a.getEmpleadoAsignado() == null);
		this.calculoPagoPorTarea = (t) -> {
			 double costo=0;

			int horasTareaRealizada = (t.getFechaFin().getDayOfMonth() - t.getFechaFin().getDayOfMonth()) * 4;

			if (horasTareaRealizada < t.getDuracionEstimada()) {
				if (this.tipo == Tipo.EFECTIVO) {
					 costo = t.getEmpleadoAsignado().getCostoHora() * 1.3;
				}
				if (this.tipo == Tipo.CONTRATADO) {
					 costo = t.getEmpleadoAsignado().getCostoHora() * 1.2;
				}
			} else {
				if( this.tipo == Tipo.CONTRATADO && (horasTareaRealizada - t.getDuracionEstimada()) > 2 ) {
					
					costo= t.getEmpleadoAsignado().getCostoHora() * 0.75;
				}
				else {
					costo = t.getEmpleadoAsignado().getCostoHora();
				}
			}

			return  horasTareaRealizada * costo;
			
			
		};
	}

	public Integer getCuil() {
		return cuil;
	}

	public void setCuil(Integer cuil) {
		this.cuil = cuil;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public void setTipo(Tipo tipo) {
		this.tipo = tipo;
	}

	public Double getCostoHora() {
		return costoHora;
	}

	public void setCostoHora(Double costoHora) {
		this.costoHora = costoHora;
	}

	public List<Tarea> getTareasAsignadas() {
		return tareasAsignadas;
	}

	public void setTareasAsignadas(List<Tarea> tareasAsignadas) {
		this.tareasAsignadas = tareasAsignadas;
	}

	public Function<Tarea, Double> getCalculoPagoPorTarea() {
		return calculoPagoPorTarea;
	}

	public void setCalculoPagoPorTarea(Function<Tarea, Double> calculoPagoPorTarea) {
		this.calculoPagoPorTarea = calculoPagoPorTarea;
	}

	public Predicate<Tarea> getPuedeAsignarTarea() {
		return puedeAsignarTarea;
	}

	public void setPuedeAsignarTarea(Predicate<Tarea> puedeAsignarTarea) {
		this.puedeAsignarTarea = puedeAsignarTarea;
	}

	public Double salario() {
		double salario = 0;
		// cargar todas las tareas no facturadas
		
		List<Tarea> tareasNoFacturadas = this.tareasAsignadas.stream()
															 .filter(t -> t.getFacturada() == null)
															 .collect(Collectors.toList());
		// calcular el costo
		// marcarlas como facturadas.
		
		if(!tareasNoFacturadas.isEmpty()) {
			for (Tarea tarea : tareasNoFacturadas) {
				salario += this.costoTarea(tarea);
				tarea.setFacturada(true);
			}

		}
		
		return salario;
	}

	/**
	 * Si la tarea ya fue terminada nos indica cuaal es el monto según el algoritmo
	 * de calculoPagoPorTarea Si la tarea no fue terminada simplemente calcula el
	 * costo en base a lo estimado.
	 * 
	 * @param t
	 * @return
	 */
	

	public Double costoTarea(Tarea t) {
		
		if(t.getFechaFin() != null) return this.calculoPagoPorTarea.apply(t);
									return  t.getDuracionEstimada() * this.costoHora;
		
	}
	
	private int horasPendientes() {
		return this.tareasAsignadas.stream()
								   .filter(t -> t.getFechaFin() == null)
								   .mapToInt(t -> t.getDuracionEstimada())
								   .sum();
			
	}

	private long tareasPendientes() {
		return this.tareasAsignadas.stream()
								   .filter(t -> t.getFechaFin() == null)
								   .count();
	}
	
	public Boolean asignarTarea(Tarea t) throws AsignacionIncorrectaException, TareaException {

		if (t.getFechaFin() != null)
			throw new AsignacionIncorrectaException("Asignacion incorrecta: Tarea finalizada");
		if (t.getEmpleadoAsignado() != null)
			throw new AsignacionIncorrectaException("Asignacion incorrecta: Ya hay un empleado asignado");

		
		// Si es contratado, no puede tener más de 5 tareas asignadas pendientes de
		// finalizar.
		
		if (this.tipo == Tipo.CONTRATADO) {
			setPuedeAsignarTarea(tarea -> this.tareasPendientes() < 5);
		}
		
		// Si es Efectivo, no puede tener asignadas, tareas pendientes de finalizar
		// que sumen más de 15 horas de trabajo estimadas.

		
		else if (this.tipo == Tipo.EFECTIVO) {
			setPuedeAsignarTarea(tarea -> this.horasPendientes() < 15);
		}
		
		if (puedeAsignarTarea.test(t)) {
			this.tareasAsignadas.add(t);
			t.asignarEmpleado(this);
			
			return true;
		}

		return false;
	}

	public void comenzar(Integer idTarea) throws TareaException {
		// busca la tarea en la lista de tareas asignadas
		// si la tarea no existe lanza una excepción
		// si la tarea existe indica como fecha de inicio la fecha y hora actual
		
		Optional<Tarea> tarea = this.tareasAsignadas.stream()
													.filter(t -> t.getId().equals(idTarea))
													.findFirst();
		String fechaActual = LocalDateTime.now().toString();
		
		if(tarea.isPresent()) {
			tarea.get().setFechaInicio(LocalDateTime.now());
			
		}else {
			throw new TareaException("Error: No se encuentra la tarea ");
		}
	}

	public void finalizar(Integer idTarea) throws TareaException{
		// busca la tarea en la lista de tareas asignadas
		// si la tarea no existe lanza una excepción
		// si la tarea existe indica como fecha de finalizacion la fecha y hora actual
		
		Optional<Tarea> tarea = this.tareasAsignadas.stream()
													.filter(t -> t.getId().equals(idTarea))
													.findFirst();
		
		String fechaActual = LocalDateTime.now().toString();

		if (tarea.isPresent()) {
			tarea.get().setFechaFin(LocalDateTime.now());

		} else {
			throw new TareaException("Error: No se encuentra la tarea ");
		}
	}

	public void comenzar(Integer idTarea, String fecha) throws TareaException {
		// busca la tarea en la lista de tareas asignadas
		// si la tarea no existe lanza una excepción
		// si la tarea existe indica como fecha de finalizacion la fecha y hora actual
		

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		LocalDateTime fechaInicio = LocalDateTime.parse(fecha, formatter);

		Optional<Tarea> tareaOptional = this.tareasAsignadas.stream()
															.filter(t -> t.getId() == idTarea)
															.findAny();
		if (tareaOptional.isPresent()) {
			tareaOptional.get().setFechaInicio(fechaInicio);
		} else {
			throw new TareaException("Error: La tarea no se encuentra");
		}

	}

	public void finalizar(Integer idTarea, String fecha) throws TareaException {
		// busca la tarea en la lista de tareas asignadas
		// si la tarea no existe lanza una excepción
		// si la tarea existe indica como fecha de finalizacion la fecha y hora actual
		
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		LocalDateTime fechaFin = LocalDateTime.parse(fecha, formatter);
		
		Optional<Tarea> tareaOptional = this.tareasAsignadas.stream()
															.filter(t -> t.getId() == idTarea)
															.findAny();
		if (tareaOptional.isPresent()) {
			tareaOptional.get().setFechaFin(fechaFin);
		} else {
			throw new TareaException("Error: La tarea no se encuentra");
		}

	}

}
