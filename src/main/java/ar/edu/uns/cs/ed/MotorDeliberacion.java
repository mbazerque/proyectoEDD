package ar.edu.uns.cs.ed;

import ar.edu.uns.cs.ed.tdas.Entry;
import ar.edu.uns.cs.ed.tdas.Position;
import ar.edu.uns.cs.ed.tdas.excepciones.EmptyStackException;
import ar.edu.uns.cs.ed.tdas.excepciones.InvalidPositionException;
import ar.edu.uns.cs.ed.tdas.tdaarbol.Arbol;
import ar.edu.uns.cs.ed.tdas.tdaarbol.Tree;
import ar.edu.uns.cs.ed.tdas.tdadiccionario.DiccionarioHashAbierto;
import ar.edu.uns.cs.ed.tdas.tdalista.ListaDoblementeEnlazada;
import ar.edu.uns.cs.ed.tdas.tdalista.PositionList;
import ar.edu.uns.cs.ed.tdas.tdapila.PilaConLista;
import ar.edu.uns.cs.ed.tdas.tdapila.Stack;

/**
 * Incluir los import necesarios para las clases utilizadas en el código.
 */

/**
 * Clase que representa el motor de deliveración.
 * 
 */
public class MotorDeliberacion {

    /**
     * Incluir los atributos necesarios para el desarrollo.
     */
    // Árbol general
    private Tree<Opinion> arbol;

    // Diccionario para indexar cada nodo por su ID y permitir búsquedas/inserciones directas en O(1).
    private DiccionarioHashAbierto<String, Position<Opinion>> diccNodos;

    // Diccionario para indexar las opiniones de cada usuario por su ID y permitir consultas en O(1).
    private DiccionarioHashAbierto<String, PositionList<Opinion>> diccUsuarios;


    // Diccionario que guarda, para cada opinión (por su id), su posición dentro de la lista de su usuario.
    // Permite sacarla de esa lista en O(1) al eliminar o deshacer, sin tener que recorrerla.
    private DiccionarioHashAbierto<String,Position<Opinion>> diccPosUsuarios;

    // Pila para registrar el historial de operaciones reversibles
    private Stack<Operaciones> pilaOperaciones;

    // Bandera para registrar si la última operación realizada fue una eliminación (la cual no se puede deshacer).
    private boolean ultimaOperacionFueEliminacion;

    /**
     * Constructor de la clase MotorDeliberacion.
     * Inicializa el árbol de debate, y las estructuras correspondientes.  
     * 
     */
    public MotorDeliberacion() {
        this.arbol = new Arbol<Opinion>();
        this.diccNodos = new DiccionarioHashAbierto<String, Position<Opinion>>();
        this.diccUsuarios = new DiccionarioHashAbierto<String, PositionList<Opinion>>();
        this.diccPosUsuarios = new DiccionarioHashAbierto<String, Position<Opinion>>();
        this.pilaOperaciones = new PilaConLista<Operaciones>();
        this.ultimaOperacionFueEliminacion = false;

    }

    /**
     * Registra una operación en la estructura que almacena las operaciones.
     * Las siguientes operaciones se pueden deshacer: "CREAR_DEBATE", "POSTEAR". El resto de las operaciones no se pueden deshacer.
     * 
     * La operación eliminar posteo no se puede deshacer, y si se intenta deshacer una operación cuando no hay operaciones registradas, se lanza una excepción.
     * 
     * 
     * @param operacion : La operación a registrar. 1: CREAR_DEBATE, 2: POSTEAR
     * @param detalle : Detalle de la operación, como el id del argumento o el id del usuario. El texto de la operación se almacena en mayúsculas para mantener consistencia. Por ejemplo: "CREAR_DEBATE:" + idRaiz.trim()
     * @param idArgumento : Identificador del argumento o de root en caso de ser el tema raíz.
     * @throws IllegalArgumentException si la operación es nula o vacía.
     */
    public void registrarOperacion(int operacion, String detalle, String idArgumento) {
        if (operacion != 1 && operacion != 2) {
            throw new IllegalArgumentException("Código de operación inválido: " + operacion);
        }
        if (detalle == null || detalle.trim().isEmpty()) {
            throw new IllegalArgumentException("El detalle de la operación no puede ser nulo ni vacío.");
        }
        if (idArgumento == null || idArgumento.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del argumento no puede ser nulo ni vacío.");
        }
        pilaOperaciones.push(new Operaciones(operacion, detalle.trim(), idArgumento.trim()));
    }

/**
 * Deshace la última operación registrada en el motor de deliberación.
 * La operación eliminar posteo no se puede deshacer, y si se intenta deshacer una operación cuando no hay operaciones registradas, se lanza una excepción.
 * @return
 * @throws EmptyStackException
 */
    public String deshacerUltimaOperacion() throws EmptyStackException {
        if (pilaOperaciones.isEmpty()) {
            throw new EmptyStackException("No hay operaciones registradas para deshacer.");
        }

        Operaciones op = pilaOperaciones.top();
        String tipoOp = (op.getOperacion() == 1) ? "CREAR_DEBATE" : "POSTEAR";

        if (ultimaOperacionFueEliminacion) {
            throw new IllegalStateException("No se pudo deshacer la operación " + tipoOp + ": no es posible deshacer tras una eliminación.");
        }

        pilaOperaciones.pop();

        if (op.getOperacion() == 1) {
            this.arbol = new Arbol<Opinion>();
            this.diccNodos = new DiccionarioHashAbierto<String, Position<Opinion>>();
            this.diccUsuarios = new DiccionarioHashAbierto<String, PositionList<Opinion>>();
            this.diccPosUsuarios = new DiccionarioHashAbierto<String, Position<Opinion>>();
            return op.getDetalle();
        } else if (op.getOperacion() == 2) {
            String idPosteo = op.getId();
            Entry<String, Position<Opinion>> entryNodo = diccNodos.find(idPosteo);
            if (entryNodo != null) {
                Position<Opinion> posNodo = entryNodo.getValue();
                Opinion opinion = posNodo.element();

                try {
                    arbol.removeNode(posNodo);
                } catch (InvalidPositionException e) {
                    throw new IllegalStateException("No se pudo deshacer la operación POSTEAR: error al remover del árbol.");
                }

                diccNodos.remove(entryNodo);

                String idUsr = opinion.getUsuario().getId();
                Entry<String, PositionList<Opinion>> entryUsr = diccUsuarios.find(idUsr);
                if (entryUsr != null) {
                    PositionList<Opinion> listaUsr = entryUsr.getValue();
                    try {
                        for (Position<Opinion> p : listaUsr.positions()) {
                            if (p.element().getIdArgumento().equals(idPosteo)) {
                                listaUsr.remove(p);
                                break;
                            }
                        }
                    } catch (InvalidPositionException e) {
                        // Posición ya no válida
                    }
                }
            }
            return op.getDetalle();
        }

        return op.getDetalle();
    }
/**
 * Crea un nuevo debate con un argumento raíz.
 * @param idRaiz   : Identificador único del tema raíz.
 * @param texto:    Texto del tema raíz.
 * @param idUsuario: Identificador del usuario.
 * @param nombreUsuario: Nombre del usuario.
 * 
 * El método valida que los parámetros no sean nulos o vacíos, y que el identificador de la raíz sea numérico.
 * Si el árbol ya tiene un debate, lanza una excepción.
 * El método registra la operación en la pila de operaciones para permitir deshacerla posteriormente.
 * @throws IllegalArgumentException si algún parámetro es inválido.
 * @throws IllegalStateException si ya existe un debate en curso.
 * Crea un usuario y un tema raíz, y los agrega al árbol y a las estructuras correspondientes.
 */
    public void crearDebate(String idRaiz, String texto, String idUsuario, String nombreUsuario) {
        if (idRaiz == null || idRaiz.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador de la raíz no puede ser nulo ni vacío.");
        }
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto del debate no puede ser nulo ni vacío.");
        }
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del usuario no puede ser nulo ni vacío.");
        }
        if (nombreUsuario == null || nombreUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del usuario no puede ser nulo ni vacío.");
        }
        if (arbol != null && !arbol.isEmpty()) {
            throw new IllegalStateException("Ya existe un debate en curso.");
        }

        Usuario usuario = new Usuario(idUsuario.trim(), nombreUsuario.trim());
        Opinion opinionRaiz = new Opinion(idRaiz.trim(), texto.trim(), "ROOT", usuario);

        arbol.createRoot(opinionRaiz);
        Position<Opinion> posRaiz = arbol.root();

        diccNodos.insert(idRaiz.trim(), posRaiz);

        Entry<String, PositionList<Opinion>> entryUsr = diccUsuarios.find(idUsuario.trim());
        PositionList<Opinion> listaUsuario;
        if (entryUsr == null) {
            listaUsuario = new ListaDoblementeEnlazada<Opinion>();
            diccUsuarios.insert(idUsuario.trim(), listaUsuario);
        } else {
            listaUsuario = entryUsr.getValue();
        }
        listaUsuario.addLast(opinionRaiz);
        ultimaOperacionFueEliminacion = false;
        registrarOperacion(1, "CREAR_DEBATE:" + idRaiz.trim(), idRaiz.trim());
    }

    public void Postear(Usuario usr, String idNuevo, String idPadre, String postura, String texto) {
        if (arbol == null || arbol.isEmpty()) {
            throw new IllegalStateException("No hay un debate activo.");
        }
        if (usr == null || usr.getId() == null || usr.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("El usuario es inválido.");
        }
        if (idNuevo == null || idNuevo.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del posteo no puede ser nulo ni vacío.");
        }
        if (idPadre == null || idPadre.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador del padre no puede ser nulo ni vacío.");
        }
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El texto del posteo no puede ser nulo ni vacío.");
        }
        if (postura == null || postura.trim().isEmpty()) {
            throw new IllegalArgumentException("La postura no puede ser nula ni vacía.");
        }

        String posturaNorm = postura.trim().toUpperCase().replace(" ", "");
        if (!posturaNorm.equals("AFAVOR") && !posturaNorm.equals("ENCONTRA")) {
            throw new IllegalArgumentException("Postura inválida: debe ser AFAVOR o ENCONTRA.");
        }

        String idNuevoTrim = idNuevo.trim();
        String idPadreTrim = idPadre.trim();

        if (diccNodos.find(idNuevoTrim) != null) {
            throw new IllegalArgumentException("Ya existe una opinión con el id: " + idNuevoTrim);
        }

        Entry<String, Position<Opinion>> entryPadre = diccNodos.find(idPadreTrim);
        if (entryPadre == null) {
            throw new IllegalArgumentException("El nodo padre no existe: " + idPadreTrim);
        }

        Position<Opinion> posPadre = entryPadre.getValue();
        Opinion nuevaOpinion = new Opinion(idNuevoTrim, texto.trim(), posturaNorm, usr);

        try {
            Position<Opinion> posHijo = arbol.addLastChild(posPadre, nuevaOpinion);
            diccNodos.insert(idNuevoTrim, posHijo);

            String idUsrTrim = usr.getId().trim();
            Entry<String, PositionList<Opinion>> entryUsr = diccUsuarios.find(idUsrTrim);
            PositionList<Opinion> listaUsuario;
            if (entryUsr == null) {
                listaUsuario = new ListaDoblementeEnlazada<Opinion>();
                diccUsuarios.insert(idUsrTrim, listaUsuario);
            } else {
                listaUsuario = entryUsr.getValue();
            }
            listaUsuario.addLast(nuevaOpinion);
            diccPosUsuarios.insert(idNuevoTrim, listaUsuario.last());

            ultimaOperacionFueEliminacion = false;
            registrarOperacion(2, "POSTEAR:" + idNuevoTrim, idNuevoTrim);
        } catch (InvalidPositionException e) {
            throw new IllegalArgumentException("Posición del padre inválida: " + e.getMessage());
        }
    }
    

    /**
     * Elimina un posteo y todo su subárbol de respuestas (eliminación en cascada).
     * Solo el autor del posteo puede eliminarlo. Si se elimina la raíz, se reinicia el debate.
     * Los nodos se eliminan en postorden, de modo que cada uno es hoja al momento de removerlo.
     * La operación no se puede deshacer.
     * Orden: O(m), siendo m la cantidad de nodos del subárbol eliminado.
     * @param idUsuario : Identificador del usuario que solicita la eliminación.
     * @param idPosteo  : Identificador del posteo a eliminar.
     * @throws IllegalArgumentException si algún id es nulo o vacío, o si el posteo no existe.
     * @throws IllegalStateException si no hay debate activo o el usuario no es el autor del posteo.
     */
    public void eliminarPosteo(String idUsuario, String idPosteo) {

        if (idUsuario == null || idUsuario.trim().isEmpty()) throw new IllegalArgumentException("Usuario nulo o vacio.");
        if (idPosteo == null || idPosteo.trim().isEmpty()) throw new IllegalArgumentException("id nulo o vacio.");
        if(arbol == null || arbol.isEmpty()) throw new IllegalStateException("No hay un debate activo.");

        Entry<String, Position<Opinion>> entrada = diccNodos.find(idPosteo.trim());
        if (entrada == null) throw new IllegalArgumentException("El posteo no existe: " + idPosteo.trim());
        
        Position<Opinion> nodo = entrada.getValue();
        String autor = nodo.element().getUsuario().getId();

        if(!(idUsuario.trim().equals(autor))) throw new IllegalStateException("El usuario " + idUsuario.trim() + " no es autor del posteo " + idPosteo.trim());
        if(arbol.isRoot(nodo)) {
            clearDebate();
            ultimaOperacionFueEliminacion = true;
            return;
        }

        PositionList<String> ids = new ListaDoblementeEnlazada<String>();
        PositionList<Position<Opinion>> posiciones = new ListaDoblementeEnlazada<Position<Opinion>>();
        obtenerIdsYPosicionesSubarbol(nodo, ids, posiciones);

        for(Position<Opinion> pos : posiciones) {
            Opinion op = pos.element();
            arbol.removeExternalNode(pos);
            
            Entry<String, Position<Opinion>> entradaPos = diccNodos.find(op.getIdArgumento());

            if(entradaPos != null) {
                diccNodos.remove(entradaPos); 
            }

            Entry<String, Position<Opinion>> entradaPosU = diccPosUsuarios.find(op.getIdArgumento());
            if(entradaPosU != null) {
                Entry<String, PositionList<Opinion>> entradaU = diccUsuarios.find(op.getUsuario().getId());
                if(entradaU != null) {
                    entradaU.getValue().remove(entradaPosU.getValue());
                }
                
                diccPosUsuarios.remove(entradaPosU);
            }


        }

        ultimaOperacionFueEliminacion = true;
    }



    //Obtiene las posiciones en postorden.
    private void obtenerIdsYPosicionesSubarbol(Position<Opinion> nodo, PositionList<String> ids, PositionList<Position<Opinion>> posiciones) {

        for(Position<Opinion> hijo : arbol.children(nodo)) {
            obtenerIdsYPosicionesSubarbol(hijo, ids, posiciones);
        }

        String id = nodo.element().getIdArgumento();
        ids.addLast(id);
        posiciones.addLast(nodo);
    }






    //limpia todo el debate, dejando la estructura vacía.   
    public void clearDebate() {
        this.arbol = new Arbol<Opinion>();
        this.diccNodos = new DiccionarioHashAbierto<String, Position<Opinion>>();
        this.diccUsuarios = new DiccionarioHashAbierto<String, PositionList<Opinion>>();
        this.diccPosUsuarios = new DiccionarioHashAbierto<String, Position<Opinion>>();
        this.pilaOperaciones = new PilaConLista<Operaciones>();
        this.ultimaOperacionFueEliminacion = false;
    }

    //consultar por usuario, devuelve una lista de argumentos que pertenecen a ese usuario.
    public PositionList<Opinion> consultarArgumentosPorUsuario(String idUsuario) {
        if (idUsuario == null || idUsuario.trim().isEmpty()) {
            throw new IllegalArgumentException("El identificador de usuario no puede ser nulo ni vacío.");
        }
        Entry<String, PositionList<Opinion>> entry = diccUsuarios.find(idUsuario.trim());
        if (entry != null && entry.getValue() != null) {
            return entry.getValue();
        }
        return new ListaDoblementeEnlazada<Opinion>();
    }

    /**
     * Calcula la polarización de todos los nodos del debate.
     * La polarización se define como la cantidad de argumentos a favor y en contra que existen en el subárbol que tiene como raíz cada nodo.
     * @throws IllegalStateException si no hay un debate activo.
     * @return Un diccionario cuya clave es el ID del nodo y el valor es un diccionario con la polarización: 
     * "AFAVOR" y "ENCONTRA" y sus respectivos conteos.
     */
    public DiccionarioHashAbierto<String,DiccionarioHashAbierto<String,Integer>> calcularPolarizacionDebate(){
        //TODO
        return null;
    }

     /**
    * Calcula la polarización de un nodo específico en el árbol de debate.
    * La polarización se define como la cantidad de argumentos a favor y en contra que existen en el subárbol que tiene como raíz el nodo especificado por idNodo.
    * @throws IllegalStateException si no hay un debate activo. 
    * @param idNodo : Identificador del nodo para el cual se desea calcular la polarización.
    * @return Un DiccionarioHashAbierto<String,Integer> que contiene la polarización del nodo, con las claves "AFAVOR" y "ENCONTRA" y sus respectivos conteos.
    */
    public DiccionarioHashAbierto<String,Integer> calcularPolarizacionNodo(String idNodo){
        //TODO
        return null;
    }
 /*** Mostrar árbol por consola ***/
 
    /**
     * Imprime por consola, en modo texto, el contenido del árbol de debate,
     * con indentación jerárquica que muestra la estructura padre-hijo.
     * Cada nodo se muestra con su id, postura, usuario y texto del argumento.
     */
    public void imprimirDebate() {
        if (arbol == null || arbol.isEmpty()) {
            System.out.println("(El árbol de debate está vacío.)");
            return;
        }

        try {
            Position<Opinion> raiz = arbol.root();
            System.out.println(formatearNodo(raiz.element()));
            imprimirHijos(raiz, "");
        } catch (Exception e) {
            System.out.println("No se pudo imprimir el árbol: " + e.getMessage());
        }
    }

    /**
     * Imprime recursivamente los hijos de 'nodo', usando 'prefijo' para
     * mantener la indentación acumulada de los niveles superiores y
     * conectores tipo "├──" / "└──" para el nivel actual.
     */
    private void imprimirHijos(Position<Opinion> nodo, String prefijo) throws InvalidPositionException {
        PositionList<Position<Opinion>> hijos = new ListaDoblementeEnlazada<Position<Opinion>>();
        for (Position<Opinion> hijo : arbol.children(nodo)) {
            hijos.addLast(hijo);
        }


        for (Position<Opinion> hijo : arbol.children(nodo)) {
            boolean esUltimo = (hijo == hijos.last().element());
            
            String conector = esUltimo ? "└── " : "├── ";
            System.out.println(prefijo + conector + formatearNodo(hijo.element()));

            String prefijoHijo = prefijo + (esUltimo ? "    " : "│   ");
            imprimirHijos(hijo, prefijoHijo);
            
        }
    }

/** Arma la línea de texto que representa a un Argumento en la impresión. */
    private String formatearNodo(Opinion a) {
        if (a == null) {
            return "(argumento nulo)";
        }
        return "[" + a.getIdArgumento() + "] (" + a.getPostura() + ") "
                + "usuario=" + a.getUsuario().getId() + " -> " + a.getTexto();
    }
}
