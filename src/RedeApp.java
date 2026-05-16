import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;

import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.URL;
import java.net.HttpURLConnection;

import java.io.*;
import java.util.*;

public class RedeApp {
	
	static abstract class ItemLista {
		String nome;
	}

    // ==============================
    // Classe PC
    // ==============================
    static class PC extends ItemLista{
        String host;
        String ip;
        String mac;
        boolean online;
		String setor;
		long lastSeen;

        PC(String nome, String host, String ip, String mac, String setor) {
            this.nome = nome;
            this.host = host;
            this.ip = ip;
            this.mac = mac;
            this.online = false;
			this.setor = setor;
			this.lastSeen = 0;
        }

        public String toString() {
            return nome;
        }
    }
	
	static class Setor extends ItemLista{
		Setor(String nome){
			this.nome = nome;
		}
		public String toString(){
			return nome;
		}
	}

    // ==============================
    // Lista de PCs
    // ==============================
    static java.util.List<PC> pcs = new java.util.ArrayList<>();
	
	static String definirSetor(String nomePC) {

		String n = nomePC.toUpperCase();

		if (n.contains("ROOM")) {
			return "Salas de Aula";
		}

		if (n.contains("SPGKS")) {
			return "Super Geeks";
		}

		return "Administrativo";
	}
	
	static void carregarPCsDoServidor() {

		try {
			URL url = new URL("http://10.224.10.45:8080/pcs");

			HttpURLConnection con =
					(HttpURLConnection) url.openConnection();

			con.setRequestMethod("GET");

			BufferedReader br = new BufferedReader(
					new InputStreamReader(con.getInputStream())
			);

			StringBuilder response = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				response.append(line);
			}

			br.close();

			String json = response.toString();

			pcs.clear();

			json = json.substring(1, json.length() - 1); // remove [ ]
			String[] itens = json.split("\\},\\{");
			
			for (String item : itens) {
				
				item = item.replace("{", "").replace("}", "");

				String host = extrair(item, "host");
				String ip = extrair(item, "ip");
				String mac = extrair(item, "mac");
				String setor = definirSetor(host);
				long lastSeen = Long.parseLong(extrairNumero(item, "lastSeen"));
				boolean online = extrairBoolean(item, "online");
				
				PC pc = new PC(host, host, ip, mac, setor);
				pc.lastSeen = lastSeen;
				pc.online = online;
				pcs.add(pc);
			}
			
			Collections.sort(pcs, (a, b) -> a.nome.compareToIgnoreCase(b.nome));
			System.out.println("PCs carregados: " + pcs.size());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void atualizarStatusDoServidor() {
		try {
			URL url = new URL("http://10.224.10.45:8080/pcs");

			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");

			BufferedReader br = new BufferedReader(
				new InputStreamReader(con.getInputStream())
			);

			StringBuilder response = new StringBuilder();
			String line;

			while ((line = br.readLine()) != null) {
				response.append(line);
			}

			String json = response.toString();

			json = json.substring(1, json.length() - 1);
			String[] itens = json.split("\\},\\{");

			Map<String, Long> mapa = new HashMap<>();

			for (String item : itens) {
				item = item.replace("{", "").replace("}", "");

				String host = extrair(item, "host");
				long lastSeen = Long.parseLong(extrairNumero(item, "lastSeen"));

				mapa.put(host, lastSeen);
			}

			long agora = System.currentTimeMillis();

			for (PC pc : pcs) {
				Long ls = mapa.get(pc.host);
				if (ls != null) {
					pc.lastSeen = ls;
					pc.online = (agora - ls) < 15000;
				} else {
					pc.online = false;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static String extrair(String json, String campo) {

		String busca = "\"" + campo + "\":\"";

		int inicio = json.indexOf(busca);

		if (inicio == -1) return "";

		inicio += busca.length();

		int fim = json.indexOf("\"", inicio);

		return json.substring(inicio, fim);
	}
	
	static String extrairNumero(String json, String campo) {

		String busca = "\"" + campo + "\":";

		int inicio = json.indexOf(busca);

		if (inicio == -1) return "0";

		inicio += busca.length();

		int fim = inicio;

		while (fim < json.length() && Character.isDigit(json.charAt(fim))) {
			fim++;
		}

		return json.substring(inicio, fim);
	}
	
	static boolean extrairBoolean(String json, String campo) {

		String busca = "\"" + campo + "\":";

		int inicio = json.indexOf(busca);

		if (inicio == -1) return false;

		inicio += busca.length();

		if (json.startsWith("true", inicio)) {
			return true;
		}

		return false;
	}
		
	static void sendWakeOnLan(String macAddress, String broadcast, int port) {
		try {
			macAddress = macAddress.replace(":", "").replace("-", "");

			byte[] macBytes = new byte[6];
			for (int i = 0; i < 6; i++) {
				macBytes[i] = (byte) Integer.parseInt(macAddress.substring(i * 2, i * 2 + 2), 16);
			}

			byte[] packet = new byte[6 + 16 * macBytes.length];

			// 6x FF
			for (int i = 0; i < 6; i++) {
				packet[i] = (byte) 0xFF;
			}

			// 16 repetições da MAC
			for (int i = 6; i < packet.length; i += macBytes.length) {
				System.arraycopy(macBytes, 0, packet, i, macBytes.length);
			}

			DatagramPacket datagram = new DatagramPacket(
				packet,
				packet.length,
				InetAddress.getByName(broadcast),
				port
			);

			DatagramSocket socket = new DatagramSocket();
			socket.send(datagram);
			socket.close();

			System.out.println("WOL enviado para " + macAddress);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	static void shutdownPC(String host) {
		try {
			ProcessBuilder pb = new ProcessBuilder(
				"shutdown",
				"/s",
				"/m", "\\\\" + host,
				"/t", "0",
				"/f"
			);

			pb.start();

			System.out.println("Desligando: " + host);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    // ==============================
    // MAIN
    // ==============================
    public static void main(String[] args) {
		
		carregarPCsDoServidor();
		
		Collections.sort(pcs, (a, b) -> a.nome.compareToIgnoreCase(b.nome));

		JFrame frame = new JFrame("Controle da Rede");
		frame.setSize(600, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// ==============================
		// Criar árvore
		// ==============================
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Rede");
		java.util.Map<String, DefaultMutableTreeNode> setores = new java.util.HashMap<>();

		for (PC pc : pcs) {

			if (!setores.containsKey(pc.setor)) {
				DefaultMutableTreeNode setorNode = new DefaultMutableTreeNode(pc.setor);
				setores.put(pc.setor, setorNode);
				root.add(setorNode);
			}

			DefaultMutableTreeNode pcNode = new DefaultMutableTreeNode(pc);
			setores.get(pc.setor).add(pcNode);
		}

		JTree tree = new JTree(root);
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);

		// ==============================
		// Renderer (cores + status)
		// ==============================
		
		ImageIcon iconOn = new ImageIcon(
			RedeApp.class.getResource("/imagens/ligar.png")
		);
		ImageIcon iconOff = new ImageIcon(
			RedeApp.class.getResource("/imagens/desligar.png")
		);
		tree.setCellRenderer(new DefaultTreeCellRenderer() {
			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value,
					boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

				JLabel label = (JLabel) super.getTreeCellRendererComponent(
						tree, value, sel, expanded, leaf, row, hasFocus);

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				Object obj = node.getUserObject();

				if (obj instanceof PC) {
					PC pc = (PC) obj;

					label.setText(pc.nome + " - " + (pc.online ? "ONLINE" : "OFFLINE"));

					label.setIcon(pc.online ? iconOn : iconOff);

					if (!sel) {
						label.setForeground(pc.online ? new Color(0,150,0) : Color.RED);
					}
				} else {
					label.setForeground(Color.BLUE);
					label.setFont(label.getFont().deriveFont(Font.BOLD));
				}

				return label;
			}
		});

		// ==============================
		// Seleção inteligente
		// ==============================
		tree.addTreeSelectionListener(e -> {

			DefaultMutableTreeNode node = (DefaultMutableTreeNode)
					tree.getLastSelectedPathComponent();

			if (node == null) return;

			Object obj = node.getUserObject();

			// Se clicou em SETOR
			if (!(obj instanceof PC)) {

				java.util.List<TreePath> paths = new java.util.ArrayList<>();

				for (int i = 0; i < node.getChildCount(); i++) {
					DefaultMutableTreeNode child =
							(DefaultMutableTreeNode) node.getChildAt(i);

					paths.add(new TreePath(child.getPath()));
				}

				tree.setSelectionPaths(paths.toArray(new TreePath[0]));
			}
		});
		
		tree.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {

				if (e.getClickCount() == 2) {

					TreePath path = tree.getPathForLocation(e.getX(), e.getY());
					if (path == null) return;

					DefaultMutableTreeNode node =
						(DefaultMutableTreeNode) path.getLastPathComponent();

					Object obj = node.getUserObject();

					if (obj instanceof PC) {
						PC pc = (PC) obj;

						try {
							Runtime.getRuntime().exec("mstsc /v:" + pc.host);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});

		JScrollPane scroll = new JScrollPane(tree);

		// ==============================
		// Função para pegar selecionados
		// ==============================
		java.util.function.Supplier<java.util.List<PC>> getSelecionados = () -> {

			java.util.List<PC> lista = new java.util.ArrayList<>();

			TreePath[] paths = tree.getSelectionPaths();
			if (paths == null) return lista;

			for (TreePath path : paths) {

				DefaultMutableTreeNode node =
						(DefaultMutableTreeNode) path.getLastPathComponent();

				Object obj = node.getUserObject();

				if (obj instanceof PC) {
					lista.add((PC) obj);
				}
			}

			return lista;
		};
		
		

		// ==============================
		// Botões
		// ==============================
		JButton btnRDP = new JButton("RDP");
		JButton btnWOL = new JButton("Ligar");
		JButton btnShutdown = new JButton("Desligar");

		JLabel statusLabel = new JLabel("Atualizando...", SwingConstants.CENTER);

		// RDP
		btnRDP.addActionListener(e -> {

			java.util.List<PC> selecionados = getSelecionados.get();

			if (selecionados.isEmpty()) {
				statusLabel.setText("Nenhum computador selecionado.");
				JOptionPane.showMessageDialog(null, "Selecione um computador!");
				return;
			}
			
			statusLabel.setText("Abrindo RDP para " + selecionados.size() + " computador(es)...");

			for (PC pc : selecionados) {
				try {
					Runtime.getRuntime().exec("mstsc /v:" + pc.host);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		// WOL
		btnWOL.addActionListener(e -> {

			java.util.List<PC> selecionados = getSelecionados.get();

			if (selecionados.isEmpty()) {
				statusLabel.setText("Nenhum computador selecionado.");
				JOptionPane.showMessageDialog(null, "Selecione um computador!");
				return;
			}
			
			statusLabel.setText("Enviando Wake-on-LAN...");

			for (PC pc : selecionados) {
				sendWakeOnLan(pc.mac, "10.224.10.255", 9);
			}
		});

		// Shutdown
		btnShutdown.addActionListener(e -> {

			java.util.List<PC> selecionados = getSelecionados.get();

			if (selecionados.isEmpty()) {
				statusLabel.setText("Nenhum computador selecionado.");
				JOptionPane.showMessageDialog(null, "Selecione um computador!");
				return;
			}
			
			statusLabel.setText("Desligando...");

			for (PC pc : selecionados) {
				try {
					shutdownPC(pc.host);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		// ==============================
		// Atualização automática
		// ==============================
		javax.swing.Timer timer = new javax.swing.Timer(5000, e -> {
			new Thread(() -> {
				atualizarStatusDoServidor();

				SwingUtilities.invokeLater(() -> {
					tree.repaint();
					statusLabel.setText("Status atualizado");
				});
			}).start();
		});

		timer.start();

		// ==============================
		// Layout
		// ==============================
		JPanel panel = new JPanel(new BorderLayout());

		JPanel botoes = new JPanel();
		botoes.add(btnWOL);
		botoes.add(btnShutdown);
		botoes.add(btnRDP);

		panel.add(scroll, BorderLayout.CENTER);
		panel.add(botoes, BorderLayout.SOUTH);
		panel.add(statusLabel, BorderLayout.NORTH);

		frame.add(panel);
		frame.setVisible(true);
	}
}