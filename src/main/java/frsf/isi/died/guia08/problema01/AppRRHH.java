package frsf.isi.died.guia08.problema01;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import frsf.isi.died.guia08.problema01.excepciones.AsignacionIncorrectaException;
import frsf.isi.died.guia08.problema01.excepciones.TareaException;
import frsf.isi.died.guia08.problema01.modelo.Empleado;
import frsf.isi.died.guia08.problema01.modelo.Empleado.Tipo;
import frsf.isi.died.guia08.problema01.modelo.Tarea;

public class AppRRHH {

	private List<Empleado> empleados;

	public void agregarEmpleadoContratado(Integer cuil, String nombre, Double costoHora) {
		// crear un empleado
		// agregarlo a la lista

		Empleado empleadoContratado = new Empleado(cuil, nombre, Tipo.CONTRATADO, costoHora);
		empleados.add(empleadoContratado);

	}

	public void agregarEmpleadoEfectivo(Integer cuil, String nombre, Double costoHora) {
		// crear un empleado
		// agregarlo a la lista
		Empleado empleadoEfectivo = new Empleado(cuil, nombre, Tipo.EFECTIVO, costoHora);
		
		empleados.add(empleadoEfectivo);
	}

	public void asignarTarea(Integer cuil, Integer idTarea, String descripcion, Integer duracionEstimada)
			throws AsignacionIncorrectaException, TareaException {
		// crear un empleado
		// con el método buscarEmpleado() de esta clase
		// agregarlo a la lista
		Tarea t = new Tarea(idTarea, descripcion, duracionEstimada);

		Optional<Empleado> optEmpleado = this.buscarEmpleado(e -> e.getCuil() == cuil);
		if (optEmpleado.isPresent()) {
			optEmpleado.get().asignarTarea(t);
		} else {
			throw new TareaException("Error: El empleado no existe en la lista");
		}
	}

	public void empezarTarea(Integer cuil, Integer idTarea) throws TareaException {
		// busca el empleado por cuil en la lista de empleados
		// con el método buscarEmpleado() actual de esta clase
		// e invoca al método comenzar tarea
		
		Optional<Empleado> optEmpleado = this.buscarEmpleado(e -> e.getCuil() == cuil);

		if (optEmpleado.isPresent()) {
			optEmpleado.get().comenzar(idTarea);
		} else {
			throw new TareaException("Error: El empleado no existe en la lista");
		}

	}

	public void terminarTarea(Integer cuil, Integer idTarea) throws TareaException {
		// crear un empleado
		// agregarlo a la lista
		
		Optional<Empleado> optEmpleado = this.buscarEmpleado(e -> e.getCuil() == cuil);

		if (optEmpleado.isPresent()) {
			optEmpleado.get().finalizar(idTarea);
		} else {
			throw new TareaException("Error: El empleado no existe en la lista");
		}
	}

	public void cargarEmpleadosContratadosCSV(String nombreArchivo) throws FileNotFoundException, IOException {
		FileInputStream fis;
		try (Reader fileReader = new FileReader(nombreArchivo)) {
			try (BufferedReader in = new BufferedReader(fileReader)) {
				String linea = null;
				while ((linea = in.readLine()) != null) {
					String[] fila = linea.split(";");
					
						this.agregarEmpleadoContratado(Integer.valueOf(fila[0]),String.valueOf(fila[1]),Double.valueOf(fila[2]));
					}
				}
			}
		}



	public void cargarEmpleadosEfectivosCSV(String nombreArchivo) throws FileNotFoundException, IOException {
		// leer datos del archivo
		// por cada fila invocar a agregarEmpleadoContratado
		
		FileInputStream fis;
		try (Reader fileReader = new FileReader(nombreArchivo)) {
			try (BufferedReader in = new BufferedReader(fileReader)) {
				String linea = null;
				while ((linea = in.readLine()) != null) {
					String[] fila = linea.split(";");
					
					this.agregarEmpleadoContratado(Integer.valueOf(fila[0]),String.valueOf(fila[1]),Double.valueOf(fila[2]));
						
					}
				}
			}
		}
	

	public void cargarTareasCSV(String nombreArchivo) throws FileNotFoundException, IOException, NumberFormatException, AsignacionIncorrectaException, TareaException {
		// leer datos del archivo
		// cada fila del archivo tendrá:
		// cuil del empleado asignado, numero de la taera, descripcion y duración
		// estimada en horas.
		
		FileInputStream fis;
		try (Reader fileReader = new FileReader(nombreArchivo)) {
			try (BufferedReader in = new BufferedReader(fileReader)) {
				String linea = null;
				while ((linea = in.readLine()) != null) {
					String[] fila = linea.split(";");
					
					this.asignarTarea(Integer.valueOf(fila[0]),Integer.valueOf(fila[1]),String.valueOf(fila[2]),Integer.valueOf(fila[3]));
						
					}
				}
			}
		
	}

	private void guardarTareasTerminadasCSV() throws IOException {
		// guarda una lista con los datos de la tarea que fueron terminadas
		// y todavía no fueron facturadas
		// y el nombre y cuil del empleado que la finalizó en formato CSV
		
		for(Empleado unEmpleado : this.empleados){
			for(Tarea unaTarea: unEmpleado.getTareasAsignadas()){
				if(unaTarea.getFacturada() == null && unaTarea.getFechaFin() != null){
					
					try(Writer fileWriter= new FileWriter("tareas.csv",true)) {
						try(BufferedWriter out = new BufferedWriter(fileWriter)){
						out.write(unaTarea.asCsv()+ System.getProperty("line.separator"));
						}
						}
				}
			}
		}
	}

	private Optional<Empleado> buscarEmpleado(Predicate<Empleado> p) {
		return this.empleados.stream().filter(p).findFirst();
	}

	public Double facturar() throws IOException {
		this.guardarTareasTerminadasCSV();
		return this.empleados.stream().mapToDouble(e -> e.salario()).sum();
	}
}
