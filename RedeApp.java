import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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

        PC(String nome, String host, String ip, String mac, String setor) {
            this.nome = nome;
            this.host = host;
            this.ip = ip;
            this.mac = mac;
            this.online = false;
			this.setor = setor;
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
    static PC[] pcs = {

		// ================= SALAS =================
		new PC("Sala 01", "KIDS-ROOM", "10.224.10.29", "00-E0-4C-A3-04-34", "Salas"),
		new PC("Sala 02", "2ND-ROOM", "10.224.10.27", "F4-8E-38-E4-92-8B", "Salas"),
		new PC("Sala 03", "3RD-ROOM", "10.224.10.14", "0C-9D-92-C7-E1-61", "Salas"),
		new PC("Sala 04", "4TH-ROOM", "10.224.10.24", "F4-8E-38-E3-68-30", "Salas"),
		new PC("Sala 05", "5TH-ROOM", "10.224.10.33", "0C-9D-92-C7-E1-39", "Salas"),
		new PC("Sala 06", "6TH-ROOM", "10.224.10.36", "4C-CC-6A-4D-DC-89", "Salas"),
		new PC("Sala 07", "7TH-ROOM", "10.224.10.16", "FC-AA-14-F7-ED-39", "Salas"),
		new PC("Sala 08", "8TH-ROOM", "10.224.10.35", "0C-9D-92-C7-E2-31", "Salas"),
		new PC("Sala 09", "TECH-ROOM", "10.224.10.32", "0C-9D-92-C7-E7-B5", "Salas"),
		new PC("Sala 10", "MAKER-ROOM", "10.224.10.48", "0C-9D-92-C7-E1-EB", "Salas"),

		// ================= SPGKS =================
		new PC("SPGKS 01", "SPGKS1", "10.224.10.20", "00-EA-7B-AB-07-7C", "SPGKS"),
		new PC("SPGKS 02", "SPGKS2", "10.224.10.53", "0C-9D-92-C7-E1-F0", "SPGKS"),
		new PC("SPGKS 03", "SPGKS3", "10.224.10.30", "0C-9D-92-C7-E1-69", "SPGKS"),
		new PC("SPGKS 04", "SPGKS4", "10.224.10.56", "0C-9D-92-C7-E1-ED", "SPGKS"),
		new PC("SPGKS 05", "SPGKS5", "10.224.10.26", "22-28-4D-01-7A-C0", "SPGKS"),
		new PC("SPGKS 06", "SPGKS6", "10.224.10.37", "5C-A6-E6-24-C3-B4", "SPGKS"),
		new PC("SPGKS 07", "SPGKS7", "10.224.10.22", "0C-9D-92-C7-E2-2B", "SPGKS"),
		new PC("SPGKS 08", "SPGKS8", "10.224.10.55", "0C-9D-92-C7-E1-10", "SPGKS"),
		new PC("SPGKS 09", "SPGKS9", "10.224.10.44", "22-01-4D-10-0D-32", "SPGKS"),
		new PC("SPGKS 10", "SPGKS10", "10.224.10.54", "0C-9D-92-C7-E0-FC", "SPGKS"),
		new PC("SPGKS 11", "SPGKS11", "10.224.10.28", "22-28-4D-01-7A-BF", "SPGKS"),
		new PC("SPGKS 12", "SPGKS12", "10.224.10.18", "B0-6E-BF-5A-F3-F7", "SPGKS"),
		new PC("SPGKS 13", "SPGKS13", "10.224.10.12", "22-34-4D-09-62-A3", "SPGKS"),

		// ================= SECRETARIA =================
		new PC("Secretaria 01", "ATENDIMENTO1", "10.224.10.42", "78-F2-9E-F3-24-97", "Secretaria"),
		new PC("Secretaria 02", "ATENDIMENTO2", "10.224.10.17", "D4-5D-DF-03-D8-EB", "Secretaria"),
		new PC("Secretaria 03", "ATENDIMENTO3", "10.224.10.15", "D4-5D-DF-03-DC-1B", "Secretaria"),
		new PC("Fiscal", "CONTROLLER", "10.224.10.23", "70-4D-7B-CE-66-9A", "Secretaria"),
		new PC("Comercial", "COMERCIAL", "10.224.10.39", "D4-5D-DF-03-D8-C0", "Secretaria"),
		new PC("Coordenacao", "COORDINATOR", "10.224.10.25", "70-4D-7B-CE-65-D8", "Secretaria"),
		new PC("RH", "MINIPC-RESERVA", "10.224.10.19", "D4-5D-DF-05-7E-E6", "Secretaria"),
		new PC("Gerencia", "GERENTE", "10.224.10.38", "D4-5D-DF-03-E3-14", "Secretaria")
	};
	
		
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
			RedeApp.class.getResource("/ligar.png")
		);
		ImageIcon iconOff = new ImageIcon(
			RedeApp.class.getResource("/desligar.png")
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
		Timer timer = new Timer(5000, e -> {
			new Thread(() -> {
				for (PC pc : pcs) {
					try {
						pc.online = InetAddress.getByName(pc.ip).isReachable(1000);
					} catch (Exception ex) {
						pc.online = false;
					}
				}

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