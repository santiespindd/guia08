package frsf.isi.died.guia08.problema01.modelo;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import frsf.isi.died.guia08.problema01.excepciones.AsignacionIncorrectaException;
import frsf.isi.died.guia08.problema01.excepciones.TareaException;
import frsf.isi.died.guia08.problema01.modelo.Empleado.*;


public class EmpleadoTest {


	
	@Test
	public void testSalario() throws AsignacionIncorrectaException, TareaException {
		
		
		
		
		
		Empleado emp1 = new Empleado(2000, "Santiago", Tipo.CONTRATADO, 100.0);
		Empleado emp2 = new Empleado(1000, "Jorge", Tipo.EFECTIVO, 60.0);
		
		Tarea t1 = new Tarea(1, "Pintar salas", 3);
		Tarea t2 = new Tarea(2, "Reparacion freezer", 1);
		
	
		emp1.asignarTarea(t1);
		
		emp1.comenzar(1);
		

		emp2.asignarTarea(t2);
		emp2.comenzar(2);
	
		assertTrue(emp1.salario() > 0 && emp2.salario() > 0);
		
		
		
		
	}

	@Test
	public void testCostoTarea() throws AsignacionIncorrectaException {
	
	}

	@Test
	public void testAsignarTarea() {
		fail("Not yet implemented");
	}

	@Test
	public void testComenzarInteger() {
		fail("Not yet implemented");
	}

	@Test
	public void testFinalizarInteger() {
		fail("Not yet implemented");
	}

	@Test
	public void testComenzarIntegerString() {
		fail("Not yet implemented");
	}

	@Test
	public void testFinalizarIntegerString() {
		fail("Not yet implemented");
	}

}
