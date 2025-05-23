import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CircuitSolverGUI es el programa principal.
 * Este programa permite ingresar un circuito (nodos, resistores, fuentes de voltaje
 * y fuentes de corriente) mediante una interfaz gráfica. Se dibuja el circuito en pantalla,
 * se arma el sistema de ecuaciones usando análisis nodal modificado y se resuelve mediante
 * eliminación gaussiana. Además, identifica resistencias en corto (cuando la diferencia de potencial
 * es cero) y muestra la matriz aumentada utilizada para resolver el sistema.
 */
public class CircuitSolverGUI extends JFrame {

    // Colecciones del circuito: nodos y elementos
    private ArrayList<Node> nodes;
    private ArrayList<CircuitElement> elements;
    // Panel de dibujo del circuito
    private CircuitPanel circuitPanel;
    // Área de texto donde se muestran resultados o mensajes
    private JTextArea outputArea;

    // Constructor: se arma la interfaz y se inicializan las colecciones.
    public CircuitSolverGUI() {
        super("Analizador de Circuitos con Eliminación Gaussiana");
        nodes = new ArrayList<>();
        elements = new ArrayList<>();
        initializeGUI();
    }

    private void initializeGUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000,700);
        setLocationRelativeTo(null);

        // Panel de controles/entradas en el lado izquierdo
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setPreferredSize(new Dimension(350, 700));

        JTabbedPane tabbedPane = new JTabbedPane();

        // Panel de entrada manual
        JPanel manualPanel = new JPanel(new GridBagLayout());
        manualPanel.setBorder(new TitledBorder("Entrada Manual"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4,4,4,4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0; gbc.gridy = 0;

        // Sección para agregar un nodo
        manualPanel.add(new JLabel("Agregar Nodo:"), gbc);
        gbc.gridy++;
        JPanel nodoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField nodeIdField = new JTextField(3);
        nodeIdField.setToolTipText("ID numérico del nodo (p.ej. 0,1,2...)");
        JTextField posXField = new JTextField(3);
        posXField.setToolTipText("Posición X (píxeles)");
        JTextField posYField = new JTextField(3);
        posYField.setToolTipText("Posición Y (píxeles)");
        nodoPanel.add(new JLabel("ID:")); nodoPanel.add(nodeIdField);
        nodoPanel.add(new JLabel("X:")); nodoPanel.add(posXField);
        nodoPanel.add(new JLabel("Y:")); nodoPanel.add(posYField);
        manualPanel.add(nodoPanel, gbc);

        gbc.gridy++;
        JButton addNodeButton = new JButton("Agregar Nodo");
        manualPanel.add(addNodeButton, gbc);

        // Separador
        gbc.gridy++;
        manualPanel.add(new JSeparator(), gbc);

        // Sección para agregar elementos
        gbc.gridy++;
        manualPanel.add(new JLabel("Agregar Elemento:"), gbc);
        gbc.gridy++;
        JPanel elementPanel = new JPanel(new GridLayout(0,2,5,5));
        // Tipo del elemento: R, V, I
        elementPanel.add(new JLabel("Tipo (R/V/I):"));
        JTextField typeField = new JTextField(1);
        elementPanel.add(typeField);
        // Nodo positivo (o de entrada)
        elementPanel.add(new JLabel("Nodo 1:"));
        JTextField node1Field = new JTextField(3);
        elementPanel.add(node1Field);
        // Nodo negativo (o de salida)
        elementPanel.add(new JLabel("Nodo 2:"));
        JTextField node2Field = new JTextField(3);
        elementPanel.add(node2Field);
        // Valor: p.ej. 4.7k, 10, 2.2M, etc.
        elementPanel.add(new JLabel("Valor:"));
        JTextField valueField = new JTextField(6);
        elementPanel.add(valueField);

        manualPanel.add(elementPanel, gbc);
        gbc.gridy++;
        JButton addElementButton = new JButton("Agregar Elemento");
        manualPanel.add(addElementButton, gbc);

        // Botón para resolver circuito
        gbc.gridy++;
        JButton solveButton = new JButton("Resolver Circuito");
        manualPanel.add(solveButton, gbc);

        // Opción para cargar desde archivo
        gbc.gridy++;
        JButton loadFileButton = new JButton("Cargar desde Archivo");
        manualPanel.add(loadFileButton, gbc);

        tabbedPane.addTab("Circuito", manualPanel);

        // Panel de resultados
        // Botón para limpiar circuito
        gbc.gridy++;
        JButton clearButton = new JButton("Limpiar Circuito");
        manualPanel.add(clearButton, gbc);
        JPanel resultsPanel = new JPanel(new BorderLayout());
        resultsPanel.setBorder(new TitledBorder("Resultados"));
        outputArea = new JTextArea(15,30);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Resultados", resultsPanel);

        controlPanel.add(tabbedPane, BorderLayout.CENTER);
        getContentPane().add(controlPanel, BorderLayout.WEST);

        // Panel de circuito (visualización)
        circuitPanel = new CircuitPanel();
        circuitPanel.setBackground(Color.WHITE);
        getContentPane().add(circuitPanel, BorderLayout.CENTER);

        // Acción para agregar un nodo
        addNodeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int id = Integer.parseInt(nodeIdField.getText().trim());
                    int x = Integer.parseInt(posXField.getText().trim());
                    int y = Integer.parseInt(posYField.getText().trim());
                    // Verificar si ya existe un nodo con el mismo id
                    for(Node n : nodes){
                        if(n.id == id) {
                            JOptionPane.showMessageDialog(null, "El nodo con id " + id + " ya existe.");
                            return;
                        }
                    }
                    Node newNode = new Node(id, x, y);
                    nodes.add(newNode);
                    circuitPanel.repaint();
                    outputArea.append("Nodo " + id + " agregado en (" + x + "," + y + ")\n");
                } catch(NumberFormatException ex){
                    JOptionPane.showMessageDialog(null, "Error en la entrada de datos para nodo.");
                }
            }
        });

        // Acción para agregar un elemento
        addElementButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tipo = typeField.getText().trim().toUpperCase();
                try {
                    int node1Id = Integer.parseInt(node1Field.getText().trim());
                    int node2Id = Integer.parseInt(node2Field.getText().trim());
                    double valor = parseValue(valueField.getText().trim());
                    
                    // Buscar los nodos por su id
                    Node n1 = findNodeById(node1Id);
                    Node n2 = findNodeById(node2Id);
                    if(n1 == null || n2 == null) {
                        JOptionPane.showMessageDialog(null, "Alguno de los nodos no existe. Primero agrégalos.");
                        return;
                    }
                    CircuitElement elem = null;
                    if(tipo.equals("R")){
                        elem = new Resistor(n1, n2, valor);
                    } else if(tipo.equals("V")){
                        elem = new VoltageSource(n1, n2, valor);
                    } else if(tipo.equals("I")){
                        elem = new CurrentSource(n1, n2, valor);
                    } else {
                        JOptionPane.showMessageDialog(null, "Tipo de elemento inválido. Use R, V o I.");
                        return;
                    }
                    elements.add(elem);
                    circuitPanel.repaint();
                    outputArea.append("Elemento " + tipo + " agregado entre N" + node1Id + " y N" + node2Id + " con valor " + valor + "\n");
                } catch(NumberFormatException ex){
                    JOptionPane.showMessageDialog(null, "Error en la entrada de datos para elemento.");
                }
            }
        });
        
        // Acción para cargar desde archivo de texto.
        loadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(".");
                int ret = chooser.showOpenDialog(CircuitSolverGUI.this);
                if(ret == JFileChooser.APPROVE_OPTION) {
                    File file = chooser.getSelectedFile();
                    loadCircuitFromFile(file);
                }
            }
        });

        // Acción para resolver el circuito
        solveButton.addActionListener(new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
        if(nodes.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay nodos ingresados.");
            return;
        }
        if(findNodeById(0)==null) {
            JOptionPane.showMessageDialog(null, "Debe existir un nodo con ID 0 (tierra).");
            return;
        }
        // --- COPIA PROFUNDA DE NODOS Y ELEMENTOS ---
        ArrayList<Node> nodesCopy = new ArrayList<>();
        HashMap<Integer, Node> idToNodeCopy = new HashMap<>();
        for(Node n : nodes) {
            Node nc = new Node(n.id, n.x, n.y);
            nodesCopy.add(nc);
            idToNodeCopy.put(n.id, nc);
        }
        ArrayList<CircuitElement> elementsCopy = new ArrayList<>();
        for(CircuitElement ce : elements) {
            Node n1 = idToNodeCopy.get(ce.node1.id);
            Node n2 = idToNodeCopy.get(ce.node2.id);
            if(ce instanceof Resistor)
                elementsCopy.add(new Resistor(n1, n2, ((Resistor)ce).resistance));
            else if(ce instanceof VoltageSource)
                elementsCopy.add(new VoltageSource(n1, n2, ((VoltageSource)ce).voltage));
            else if(ce instanceof CurrentSource)
                elementsCopy.add(new CurrentSource(n1, n2, ((CurrentSource)ce).current));
            else if(ce instanceof Cable)
                elementsCopy.add(new Cable(n1, n2));
        }
        // --- FUSIONA SOLO EN LA COPIA ---
        mergeCableConnectedNodes(nodesCopy, elementsCopy);

        CircuitSolver solver = new CircuitSolver(nodesCopy, elementsCopy);
        try {
            solver.solveCircuit();
            displayResults(solver, nodesCopy, elementsCopy);
        } catch(Exception ex){
            outputArea.append("Error al resolver el circuito: " + ex.getMessage() + "\n");
        }
    }
});

        // Acción para limpiar el circuito
    clearButton.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showConfirmDialog(
            CircuitSolverGUI.this,
            "¿Está seguro de que desea limpiar todo el circuito?",
            "Confirmar limpieza",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm == JOptionPane.YES_OPTION) {
            nodes.clear();
            elements.clear();
            outputArea.setText("");
            circuitPanel.repaint();
            outputArea.append("Circuito limpiado.\n");
        }
    }
});
    }

    // Método para encontrar un nodo dado su id.
    private Node findNodeById(int id) {
        for(Node n : nodes) {
            if(n.id == id) return n;
        }
        return null;
    }
    
    /**
     * Método para leer de un archivo.
     * Se espera que el archivo tenga líneas con el formato:
     * 
     * NODO id x y
     * ELEMENTO tipo node1 node2 valor
     * 
     * Por ejemplo:
     * NODO 0 100 300
     * NODO 1 200 300
     * ELEMENTO R 0 1 4.7k
     * ELEMENTO V 1 0 5
     */
    private void loadCircuitFromFile(File file) {
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        nodes.clear();
        elements.clear();
        while((line = br.readLine()) != null) {
                line = line.trim();
                if(line.isEmpty()) continue;
                String[] parts = line.split("\\s+");
                if(parts[0].equalsIgnoreCase("NODO") && parts.length>=4) {
                    int id = Integer.parseInt(parts[1]);
                    int x = Integer.parseInt(parts[2]);
                    int y = Integer.parseInt(parts[3]);
                    nodes.add(new Node(id, x, y));
                    outputArea.append("Nodo " + id + " agregado desde archivo.\n");
                } else if(parts[0].equalsIgnoreCase("ELEMENTO") && parts.length>=5) {
                    String tipo = parts[1].toUpperCase();
                    int node1 = Integer.parseInt(parts[2]);
                    int node2 = Integer.parseInt(parts[3]);
                    double valor = parseValue(parts[4]);
                    Node n1 = findNodeById(node1);
                    Node n2 = findNodeById(node2);
                    if(n1==null || n2==null) {
                        outputArea.append("Error: nodo referenciado en elemento no existe.\n");
                        continue;
                    }
                    CircuitElement elem = null;
                    if(tipo.equals("R")) {
                        elem = new Resistor(n1, n2, valor);
                    } else if(tipo.equals("V")) {
                        elem = new VoltageSource(n1, n2, valor);
                    } else if(tipo.equals("I")) {
                        elem = new CurrentSource(n1, n2, valor);
                    } else if(tipo.equals("C")) {
                        elem = new Cable(n1, n2);
                    }
                    if(elem!=null) {
                        elements.add(elem);
                        outputArea.append("Elemento " + tipo + " agregado desde archivo.\n");
                    }
                }
            }
            
        circuitPanel.repaint();
    } catch(Exception ex) {
        JOptionPane.showMessageDialog(null, "Error al leer el archivo: " + ex.getMessage());
    }
}

    // Método auxiliar para convertir un string con prefijos a valor double.
    // Ejemplo: "4.7k" -> 4700, "2.2M" -> 2200000, "10" -> 10.
    private double parseValue(String s) throws NumberFormatException {
        s = s.trim();
        Pattern p = Pattern.compile("([0-9.]+)\\s*([kKmMuU]?)([ohmVAI]?)");
        Matcher m = p.matcher(s);
        if(m.matches()){
            double value = Double.parseDouble(m.group(1));
            String prefix = m.group(2);
            if(prefix.equalsIgnoreCase("k"))
                value *= 1e3;
            else if(prefix.equalsIgnoreCase("m"))
                value *= 1e-3;
            else if(prefix.equalsIgnoreCase("u"))
                value *= 1e-6;
            else if(prefix.equalsIgnoreCase("M"))
                value *= 1e6;
            return value;
        } else {
            throw new NumberFormatException("Formato inválido en valor: " + s);
        }
    }

    // Muestra en el área de salida los resultados (voltajes en nodos, corrientes en elementos y la matriz aumentada)
    private void displayResults(CircuitSolver solver, ArrayList<Node> analysisNodes, ArrayList<CircuitElement> analysisElements) {
        DecimalFormat df = new DecimalFormat("#.####");
        outputArea.append("\n--- Matriz aumentada del sistema ---\n");
        outputArea.append(solver.getAugmentedMatrixString() + "\n");
    
        outputArea.append("\n--- Resultados ---\n");
        HashMap<Integer,Double> nodeVoltages = solver.getNodeVoltages();
        for(Node n : nodes) {
        int repId = findRepresentativeId(n.id, analysisNodes, analysisElements);
        Double v = nodeVoltages.get(repId);
        if (v == null) {
        outputArea.append("Nodo " + n.id + " -> V = N/A\n");
        } else {
        outputArea.append("Nodo " + n.id + " -> V = " + df.format(v) + " V\n");
    }
}

        outputArea.append("\nCorrientes por elemento:\n");
        for(CircuitElement elem : elements) {
        if(elem instanceof Resistor) {
            int repId1 = findRepresentativeId(elem.node1.id, analysisNodes, analysisElements);
            int repId2 = findRepresentativeId(elem.node2.id, analysisNodes, analysisElements);
            double v1 = (repId1 == 0 ? 0 : nodeVoltages.get(repId1));
            double v2 = (repId2 == 0 ? 0 : nodeVoltages.get(repId2));
            double diff = v1 - v2;
            double current = diff/((Resistor)elem).resistance;
            outputArea.append("Resistor entre N" + elem.node1.id + " y N" + elem.node2.id +
                    ": I = " + df.format(current) + " A, Vdrop = " + df.format(diff) + " V");
            if(Math.abs(diff) < 1e-6) {
                outputArea.append("  --> En corto");
            }
            outputArea.append("\n");
        }
        // Se pueden agregar cálculos para fuentes si se desea.
    }
}

    // Panel de dibujo del circuito; dibuja nodos y elementos.
     class CircuitPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Primero dibuja los nodos
            for(Node n : nodes) {
                g.setColor(Color.BLUE);
                g.fillOval(n.x-8, n.y-8, 16, 16);
                g.setColor(Color.BLACK);
                g.drawString("N" + n.id, n.x-15, n.y-10);
            }
            // Luego dibuja los elementos (línea entre nodos con etiqueta)
            for(CircuitElement elem : elements) {
                int x1 = elem.node1.x;
                int y1 = elem.node1.y;
                int x2 = elem.node2.x;
                int y2 = elem.node2.y;
                if(elem instanceof Cable) {
                    g.setColor(Color.GREEN);
                    drawCable(g, x1, y1, x2, y2);
                    g.drawString("Cable", (x1+x2)/2, (y1+y2)/2);
                } else if(elem instanceof Resistor) {
                    g.setColor(Color.RED);
                    drawResistor(g, x1, y1, x2, y2);
                    g.drawString("R="+elem.getFormattedValue(), (x1+x2)/2, (y1+y2)/2);
                } else if(elem instanceof VoltageSource) {
                    g.setColor(Color.MAGENTA);
                    drawVoltageSource(g, x1, y1, x2, y2);
                    g.drawString("V="+elem.getFormattedValue(), (x1+x2)/2, (y1+y2)/2);
                } else if(elem instanceof CurrentSource) {
                    g.setColor(Color.ORANGE);
                    drawCurrentSource(g, x1, y1, x2, y2);
                    g.drawString("I="+elem.getFormattedValue(), (x1+x2)/2, (y1+y2)/2);
                }
            }
        }

        // Dibuja un cable como una línea verde
        private void drawCable(Graphics g, int x1, int y1, int x2, int y2) {
            g.drawLine(x1, y1, x2, y2);
        }
        // Métodos simples para dibujar cada símbolo. (Las representaciones son esquemáticas).
        private void drawResistor(Graphics g, int x1, int y1, int x2, int y2) {
            g.drawLine(x1, y1, x2, y2);
        }
        private void drawVoltageSource(Graphics g, int x1, int y1, int x2, int y2) {
            g.drawLine(x1, y1, x2, y2);
            // Dibuja un pequeño círculo en el centro
            int cx = (x1+x2)/2;
            int cy = (y1+y2)/2;
            g.drawOval(cx-10, cy-10, 20,20);
        }
        private void drawCurrentSource(Graphics g, int x1, int y1, int x2, int y2) {
            g.drawLine(x1, y1, x2, y2);
            // Dibuja una flecha en el centro
            int cx = (x1+x2)/2;
            int cy = (y1+y2)/2;
            g.drawLine(cx, cy, cx+5, cy+5);
            g.drawLine(cx, cy, cx-5, cy+5);
        }
    }

    // Clase para representar nodos
    static class Node {
        int id;
        int x, y;
        public Node(int id, int x, int y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }
    }

    // Clase abstracta para elementos del circuito.
    static abstract class CircuitElement {
        Node node1, node2;
        double value; // Valor numérico correspondiente al elemento
        public CircuitElement(Node node1, Node node2, double value) {
            this.node1 = node1;
            this.node2 = node2;
            this.value = value;
        }
        public abstract String getFormattedValue();
    }

    // Resistor
    static class Resistor extends CircuitElement {
        double resistance;
        public Resistor(Node node1, Node node2, double resistance) {
            super(node1, node2, resistance);
            this.resistance = resistance;
        }
        @Override
        public String getFormattedValue() {
            return formatWithUnit(resistance, "ohm");
        }
    }

    // Fuente de voltaje
    static class VoltageSource extends CircuitElement {
        double voltage;
        public VoltageSource(Node node1, Node node2, double voltage) {
            super(node1, node2, voltage);
            this.voltage = voltage;
        }
        @Override
        public String getFormattedValue() {
            return voltage + " V";
        }
    }

    // Fuente de corriente
    static class CurrentSource extends CircuitElement {
        double current;
        public CurrentSource(Node node1, Node node2, double current) {
            super(node1, node2, current);
            this.current = current;
        }
        @Override
        public String getFormattedValue() {
            return current + " A";
        }
    }

    // Método auxiliar para dar formato a números con unidades usando notación SI simple.
    private static String formatWithUnit(double value, String unit) {
        if(value >= 1e6)
            return (value/1e6) + " M" + unit;
        else if(value >= 1e3)
            return (value/1e3) + " k" + unit;
        else if(value < 1)
            return (value*1e3) + " m" + unit;
        else
            return value + " " + unit;
    }

    /**
     * Clase que arma y resuelve el sistema de ecuaciones del circuito usando el análisis nodal modificado (MNA).
     * La incógnita es, para cada nodo (excepto el de referencia, que se fija en 0) su tensión,
     * y para cada fuente de voltaje una incógnita adicional (la corriente a través de la fuente).
     * Se almacena la matriz aumentada en la variable augmentedMatrixString para visualizarla.
     */
    static class CircuitSolver {
        ArrayList<Node> nodes;
        ArrayList<CircuitElement> elements;
        // Mapa de voltajes resultantes por nodo (clave = id de nodo)
        HashMap<Integer,Double> nodeVoltages;
        // Listas separadas de fuentes de voltaje y corriente
        ArrayList<VoltageSource> voltageSources;
        ArrayList<CurrentSource> currentSources;
        // Variable para almacenar la matriz aumentada (como cadena) que se usó para resolver el sistema.
        private String augmentedMatrixString;
        
        public CircuitSolver(ArrayList<Node> nodes, ArrayList<CircuitElement> elements) {
            // Se hace una copia de la lista original (puede modificarse)
            this.nodes = nodes;
            this.elements = elements;
            voltageSources = new ArrayList<>();
            currentSources = new ArrayList<>();
            nodeVoltages = new HashMap<>();
            // Se extraen las fuentes de voltaje y las de corriente del listado general
            for(CircuitElement ce : elements) {
                if(ce instanceof VoltageSource)
                    voltageSources.add((VoltageSource) ce);
                if(ce instanceof CurrentSource)
                    currentSources.add((CurrentSource) ce);
            }
        }
        
        // Ejecuta el método de resolución y almacena los potenciales en nodeVoltages.
        public void solveCircuit() throws Exception {
            // Suponemos que el nodo con id 0 es tierra (V=0).
            // Se crean índices para los nodos no tierra.
            HashMap<Integer,Integer> nodeIndex = new HashMap<>();
            int index = 0;
            for(Node n : nodes) {
                if(n.id != 0) {
                    nodeIndex.put(n.id, index);
                    index++;
                }
            }
            int nNodes = nodeIndex.size(); 
            int nVoltages = voltageSources.size();
            int nEquations = nNodes + nVoltages;
            
            // Se arma la matriz A (nEquations x nEquations) y el vector b.
            double[][] A = new double[nEquations][nEquations];
            double[] b = new double[nEquations];
            
            // Primera parte: ecuaciones de KCL para cada nodo (excepto tierra)
            for(Node n : nodes) {
                if(n.id == 0) continue;
                int eq = nodeIndex.get(n.id);
                // Para resistores: se suman las conductancias
                for(CircuitElement ce : elements) {
                    if(ce instanceof Resistor) {
                        Resistor r = (Resistor) ce;
                        if(r.node1.id == n.id || r.node2.id == n.id) {
                            double g = 1.0 / r.resistance;
                            A[eq][eq] += g;
                            int other = (r.node1.id == n.id ? r.node2.id : r.node1.id);
                            if(other != 0 && nodeIndex.containsKey(other)) {
                                int col = nodeIndex.get(other);
                                A[eq][col] -= g;
                            }
                        }
                    }
                }
                // Fuentes de corriente: inyecciones o extracciones de corriente
                for(CircuitElement ce : elements) {
                    if(ce instanceof CurrentSource) {
                        CurrentSource cs = (CurrentSource) ce;
                        if(cs.node1.id == n.id)
                            b[eq] -= cs.current; // corriente que sale
                        if(cs.node2.id == n.id)
                            b[eq] += cs.current; // corriente que entra
                    }
                }
                // Fuentes de voltaje conectadas al nodo
                for (int k=0; k<voltageSources.size(); k++) {
                    VoltageSource vs = voltageSources.get(k);
                    if(vs.node1.id == n.id) {
                        A[eq][nNodes + k] += 1;
                    }
                    if(vs.node2.id == n.id) {
                        A[eq][nNodes + k] -= 1;
                    }
                }
            }
            
            // Segunda parte: ecuaciones para cada fuente de voltaje:
            // Imponen: V(node1) - V(node2) = vs.voltage
            for (int k=0; k<voltageSources.size(); k++) {
                VoltageSource vs = voltageSources.get(k);
                int eq = nNodes + k;
                if(vs.node1.id != 0 && nodeIndex.containsKey(vs.node1.id)) {
                    A[eq][nodeIndex.get(vs.node1.id)] = 1;
                }
                if(vs.node2.id != 0 && nodeIndex.containsKey(vs.node2.id)) {
                    A[eq][nodeIndex.get(vs.node2.id)] = -1;
                }
                b[eq] = vs.voltage;
            }
            
            // Se arma la matriz aumentada (para mostrarla) a partir de A y b.
            double[][] aug = new double[nEquations][nEquations+1];
            for (int i=0; i<nEquations; i++){
                for (int j=0; j<nEquations; j++){
                    aug[i][j] = A[i][j];
                }
                aug[i][nEquations] = b[i];
            }
            augmentedMatrixString = matrixToString(aug);
            
            // Se resuelve el sistema: X = (V1, V2, ..., VN, I1, I2, ...).
            double[] sol = gaussianElimination(A, b);
            
            // Se almacena el resultado asignando V=0 para el nodo tierra (id 0) y los valores resueltos para los demás.
            nodeVoltages.put(0, 0.0);
            for(Integer id : nodeIndex.keySet()){
                int pos = nodeIndex.get(id);
                nodeVoltages.put(id, sol[pos]);
            }
        }
        
        public HashMap<Integer, Double> getNodeVoltages() {
            return nodeVoltages;
        }
        
        // Método para retornar la cadena que representa la matriz aumentada.
        public String getAugmentedMatrixString() {
            return augmentedMatrixString;
        }
        
        // Método que convierte una matriz a una cadena de texto para visualización.
        private String matrixToString(double[][] M) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < M.length; i++) {
                for (int j = 0; j < M[0].length; j++) {
                    sb.append(String.format("%10.4f ", M[i][j]));
                }
                sb.append("\n");
            }
            return sb.toString();
        }
        
        // Método de eliminación gaussiana para resolver el sistema lineal.
        private double[] gaussianElimination(double[][] A, double[] b) throws Exception {
            int n = b.length;
            // Construir la matriz aumentada para la eliminación
            double[][] aug = new double[n][n+1];
            for (int i=0; i<n; i++){
                for (int j=0; j<n; j++){
                    aug[i][j] = A[i][j];
                }
                aug[i][n] = b[i];
            }
            
            // Eliminación hacia adelante
            for (int i=0; i<n; i++){
                int maxRow = i;
                for (int k=i+1; k<n; k++){
                    if (Math.abs(aug[k][i]) > Math.abs(aug[maxRow][i])) {
                        maxRow = k;
                    }
                }
                double[] temp = aug[i];
                aug[i] = aug[maxRow];
                aug[maxRow] = temp;
                
                if(Math.abs(aug[i][i]) < 1e-12)
                    throw new Exception("El sistema presenta singularidad o está mal condicionado.");
                
                double pivot = aug[i][i];
                for (int j=i; j<n+1; j++){
                    aug[i][j] /= pivot;
                }
                for (int k=i+1; k<n; k++){
                    double factor = aug[k][i];
                    for (int j=i; j<n+1; j++){
                        aug[k][j] -= factor * aug[i][j];
                    }
                }
            }
            
            // Sustitución hacia atrás
            double[] x = new double[n];
            for (int i=n-1; i>=0; i--){
                x[i] = aug[i][n];
                for (int j=i+1; j<n; j++){
                    x[i] -= aug[i][j] * x[j];
                }
            }
            return x;
        }
    }

     // Clase para representar un cable (conexión ideal, resistencia cero)
    static class Cable extends CircuitElement {
        public Cable(Node node1, Node node2) {
            super(node1, node2, 0.0);
        }
        @Override
        public String getFormattedValue() {
            return "Cable";
        }
    }
    
    // Método para fusionar nodos conectados solo por cables (C)
private void mergeCableConnectedNodes(ArrayList<Node> nodes, ArrayList<CircuitElement> elements) {
    HashMap<Integer, Integer> parent = new HashMap<>();
    for (Node n : nodes) parent.put(n.id, n.id);

    java.util.function.Function<Integer, Integer> find = new java.util.function.Function<Integer, Integer>() {
        public Integer apply(Integer x) {
            if (!parent.get(x).equals(x))
                parent.put(x, this.apply(parent.get(x)));
            return parent.get(x);
        }
    };

    java.util.function.BiConsumer<Integer, Integer> union = (a, b) -> {
        int pa = find.apply(a);
        int pb = find.apply(b);
        if (pa != pb) parent.put(pa, pb);
    };

    for (CircuitElement elem : elements) {
        if (elem instanceof Cable) {
            union.accept(elem.node1.id, elem.node2.id);
        }
    }

    HashMap<Integer, Node> repNode = new HashMap<>();
    for (Node n : nodes) {
        int repId = find.apply(n.id);
        if (!repNode.containsKey(repId)) {
            repNode.put(repId, n);
        }
    }

    for (CircuitElement elem : elements) {
        elem.node1 = repNode.get(find.apply(elem.node1.id));
        elem.node2 = repNode.get(find.apply(elem.node2.id));
    }

    ArrayList<Node> newNodes = new ArrayList<>(repNode.values());
    nodes.clear();
    nodes.addAll(newNodes);
}

private Integer findRepresentativeId(int nodeId, ArrayList<Node> allNodes, ArrayList<CircuitElement> allElements) {
    HashMap<Integer, Integer> parent = new HashMap<>();
    for (Node n : allNodes) parent.put(n.id, n.id); // CORREGIDO

    java.util.function.Function<Integer, Integer> find = new java.util.function.Function<Integer, Integer>() {
        public Integer apply(Integer x) {
            if (!parent.containsKey(x)) return x; // Si no existe, regresa el mismo id
            if (!parent.get(x).equals(x))
                parent.put(x, this.apply(parent.get(x)));
            return parent.get(x);
        }
    };

    java.util.function.BiConsumer<Integer, Integer> union = (a, b) -> {
        int pa = find.apply(a);
        int pb = find.apply(b);
        if (pa != pb) parent.put(pa, pb);
    };

    for (CircuitElement elem : allElements) {
        if (elem instanceof Cable) {
            union.accept(elem.node1.id, elem.node2.id);
        }
    }
    return find.apply(nodeId);
}

    // Método main para ejecutar el programa
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CircuitSolverGUI gui = new CircuitSolverGUI();
            gui.setVisible(true);
        });
    }
}