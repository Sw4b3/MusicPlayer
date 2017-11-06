/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicplayer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Andrew
 */
public final class MusicPlayer extends javax.swing.JFrame {

    boolean playCompleted = false;
    boolean isPlaying = false;
    boolean isPause = false;
    boolean isReset = false;
    long startTime;
    Thread playbackThread;
    String currentUsersHomeDir = System.getProperty("user.home");
    String lastOpenPath = currentUsersHomeDir + File.separator + "Music\\Library";
    internalClock timer;
    functionLibrary player = new functionLibrary();
    xmlManager mLibrary;
    int i = 0;
    int index;
    Clip audioClip;
    static ArrayList FilePath = new ArrayList();
    static ArrayList songList = new ArrayList();
    static ArrayList artistList = new ArrayList();
    String artistName;
    String songName;
    String audioFilePath;
    Point mouseDownCompCoords = null;

    public MusicPlayer() {
        initComponents();
        super.setLocationRelativeTo(null);
        mLibrary = new xmlManager(playlist, songList, artistList, FilePath);
        xmlManager.xmlValidition();
        setTableDesign();
        draggablePanel();
    }

    public void listSelection(int index) {
        i = index;
        if (isPlaying == true) {
            stopPlaying();
        } else {
            isPlaying = true;
        }
        artistName = (artistList.get(i).toString());
        songName = (songList.get(i).toString());
        audioFilePath = (FilePath.get(i).toString());
        String imagePath = xmlManager.imageFilePath.get(i);
        resizeImage(imagePath);
        playBack();
    }

    public void resizeImage(String imagePath) {
        if (imagePath != "") {
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaleImage = icon.getImage().getScaledInstance(40, 40, Image.SCALE_DEFAULT);
            icon = new ImageIcon(scaleImage);
            albumCover.setIcon(icon);
        } else {
            albumCover.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/song.png")));
        }
    }

    private void openFile() {
        JFileChooser fileChooser; //= new JFileChooser();//
        if (lastOpenPath != null && !lastOpenPath.equals("")) {
            fileChooser = new JFileChooser(lastOpenPath);
        } else {
            fileChooser = new JFileChooser();
        }

        FileFilter wavFilter = new FileFilter() {
            @Override
            public String getDescription() {
                return "Sound file (*.WAV)";
            }

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    return file.getName().toLowerCase().endsWith(".wav");
                }
            }
        };

        fileChooser.setFileFilter(wavFilter);
        fileChooser.setDialogTitle("Open Audio File");
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userChoice = fileChooser.showOpenDialog(this);
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            audioFilePath = fileChooser.getSelectedFile().getAbsolutePath();
            AddForm newSong = new AddForm(audioFilePath);
            newSong.setAlwaysOnTop(true);
            newSong.setVisible(true);
            lastOpenPath = fileChooser.getSelectedFile().getParent();
            if (isPlaying || isPause) {
                stopPlaying();
                while (player.getAudioClip().isRunning()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                    }
                }
            }
        }
    }

    private void changeAudioDirectory() {
        JFileChooser fileChooser;

        if (lastOpenPath != null && !lastOpenPath.equals("")) {
            fileChooser = new JFileChooser(lastOpenPath);
        } else {
            fileChooser = new JFileChooser();
        }

        FileFilter wavFilter = new FileFilter() {
            @Override
            public String getDescription() {
                return "Sound file (*.WAV)";
            }

            @Override
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return true;
                } else {
                    return file.getName().toLowerCase().endsWith(".wav");
                }
            }
        };

        fileChooser.setFileFilter(wavFilter);
        fileChooser.setDialogTitle("Open Audio File");
        fileChooser.setAcceptAllFileFilterUsed(false);

        int userChoice = fileChooser.showOpenDialog(this);
        if (userChoice == JFileChooser.APPROVE_OPTION) {
            audioFilePath = fileChooser.getSelectedFile().getAbsolutePath();
            xmlManager.updateDirectoy(i, audioFilePath);
            playBack();
        }
    }

    private void playBack() {
        if (shuffleMenuButton.isSelected()) {
            shuffle();
        }
        timer = new internalClock(start, sliderTime);
        timer.start();
        isPlaying = true;
        playbackThread = new Thread(() -> {
            try {
                player.load(audioFilePath);
                timer.setAudioClip(player.getAudioClip());
                sliderTime.setMaximum((int) player.getClipSecondLength());
                finishTime.setText(player.getLength());
                nowPlaying.setText("Now Playing: " + songName + " - " + artistName);
                play.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pause.png")));

                if (repeatButton.isSelected()) {
                    player.setRepeatTrue();
                    player.play();
                    resetControls();
                } else {
                    player.setRepeatFalse();
                    player.play();
                }
            } catch (IOException | UnsupportedAudioFileException | LineUnavailableException ex) {
                Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
                JOptionPane.showMessageDialog(null, "No song at directory ");
                changeAudioDirectory();
            }

            if (player.getPlayContinue()) {
                nextSong();
            }
        });
        playbackThread.start();
    }

    private void stopPlaying() {
        play.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/play.png")));
        isPause = false;
        isPlaying = false;
        timer.reset();
        timer.interrupt();
        player.stop();
        playbackThread.interrupt();
    }

    private void pausePlaying() {
        play.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/play.png")));
        isPause = true;
        player.pause();
        timer.pauseTimer();
        playbackThread.interrupt();
    }

    private void resumePlaying() {
        play.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/pause.png")));
        isPause = false;
        player.resume();
        timer.resumeTimer();
        playbackThread.interrupt();
    }

    void nextSong() {
        stopPlaying();
        FilePath.get(i);
        i++;
        artistName = (artistList.get(i).toString());
        songName = (songList.get(i).toString());
        audioFilePath = (FilePath.get(i).toString());
        String imagePath = xmlManager.imageFilePath.get(i);
        resizeImage(imagePath);
        playBack();
    }

    void previousSong() {
        stopPlaying();
        FilePath.get(i);
        i--;
        artistName = (artistList.get(i).toString());
        songName = (songList.get(i).toString());
        audioFilePath = (FilePath.get(i).toString());
        String imagePath = xmlManager.imageFilePath.get(i);
        resizeImage(imagePath);
        playBack();
    }

    private void resetControls() {
        timer.reset();
        timer.interrupt();
        isPlaying = false;
        play.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/play.png")));
    }

    public void shuffle() {
        if (isPlaying == true) {
            stopPlaying();
        }
        Random randomNo = new Random();
        int max = FilePath.size();
        i = randomNo.nextInt(max);
        artistName = (artistList.get(i).toString());
        songName = (songList.get(i).toString());
        audioFilePath = (FilePath.get(i).toString());
        String imagePath = xmlManager.imageFilePath.get(i);
        resizeImage(imagePath);
    }

    public void setClassicLook() {
        Color defaultGray = new Color(240, 240, 240);
        Color lightGray = new Color(204, 204, 204);
        playlist.setGridColor(lightGray);
        playlist.setBackground(lightGray);
        playerPanel.setBackground(defaultGray);
    }

    public void setModernLook() {
        Color lightBlue = new Color(153, 204, 255);
        Color transparentWhite = new Color(255, 255, 255);
        playerPanel.setBackground(transparentWhite);
        playlist.setGridColor(transparentWhite);
    }

    public void setTableDesign() {
        Color defaultGray = new Color(240, 240, 240);
        Color lightBlue = new Color(153, 204, 255);
        playlist.getColumnModel().getColumn(0).setPreferredWidth(5);
        playlist.getColumnModel().getColumn(1).setPreferredWidth(100);
        playlist.getColumnModel().getColumn(2).setPreferredWidth(50);
        playlist.setDefaultRenderer(Object.class, new TableCellRenderer() {
            private DefaultTableCellRenderer DEFAULT_RENDERER = new DefaultTableCellRenderer();

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component component = DEFAULT_RENDERER.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (row % 2 == 0) {
                    component.setBackground(Color.WHITE);
                } else {
                    component.setBackground(defaultGray);
                }
                return component;
            }
        });
    }

    public void draggablePanel() {
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                mouseDownCompCoords = null;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                mouseDownCompCoords = e.getPoint();
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                Point currCoords = e.getLocationOnScreen();
                setLocation(currCoords.x - mouseDownCompCoords.x, currCoords.y - mouseDownCompCoords.y);
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        playerPanel = new javax.swing.JPanel();
        play = new javax.swing.JButton();
        repeatButton = new javax.swing.JRadioButton();
        start = new javax.swing.JLabel();
        nowPlaying = new javax.swing.JLabel();
        sliderTime = new javax.swing.JSlider();
        next = new javax.swing.JButton();
        previous = new javax.swing.JButton();
        finishTime = new javax.swing.JLabel();
        jScrollPane = new javax.swing.JScrollPane();
        playlist = new javax.swing.JTable();
        albumCover = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        file = new javax.swing.JMenu();
        openMenu = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        closeMenu = new javax.swing.JMenuItem();
        controls = new javax.swing.JMenu();
        shuffleMenuButton = new javax.swing.JRadioButtonMenuItem();
        repeatMenu = new javax.swing.JMenuItem();
        resueMenu = new javax.swing.JMenuItem();
        pauseMenu = new javax.swing.JMenuItem();
        stopMenu = new javax.swing.JMenuItem();
        skipMenu = new javax.swing.JMenuItem();
        settings = new javax.swing.JMenu();
        LookandFeelButton1 = new javax.swing.JMenuItem();
        LookandFeelButton2 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        changePlayMenu = new javax.swing.JMenuItem();
        LookandFeelButton3 = new javax.swing.JMenuItem();
        help = new javax.swing.JMenu();
        patchNotes = new javax.swing.JMenuItem();
        readme = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        jMenuItem2.setText("jMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setUndecorated(true);

        playerPanel.setBackground(new java.awt.Color(253, 253, 253));
        playerPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        play.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/play.png"))); // NOI18N
        play.setBorderPainted(false);
        play.setContentAreaFilled(false);
        play.setPreferredSize(new java.awt.Dimension(45, 45));
        play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playActionPerformed(evt);
            }
        });

        repeatButton.setText("Repeat");
        repeatButton.setContentAreaFilled(false);
        repeatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                repeatButtonActionPerformed(evt);
            }
        });

        start.setText("00:00:00");

        nowPlaying.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
        nowPlaying.setText("Now Playing:                                           ");

        sliderTime.setBackground(new java.awt.Color(255, 255, 255));
        sliderTime.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        next.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/next.png"))); // NOI18N
        next.setBorderPainted(false);
        next.setContentAreaFilled(false);
        next.setMaximumSize(new java.awt.Dimension(45, 45));
        next.setMinimumSize(new java.awt.Dimension(45, 45));
        next.setPreferredSize(new java.awt.Dimension(45, 45));
        next.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextActionPerformed(evt);
            }
        });

        previous.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/previous.png"))); // NOI18N
        previous.setContentAreaFilled(false);
        previous.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        previous.setDefaultCapable(false);
        previous.setMaximumSize(new java.awt.Dimension(45, 45));
        previous.setMinimumSize(new java.awt.Dimension(45, 45));
        previous.setPreferredSize(new java.awt.Dimension(45, 45));
        previous.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousActionPerformed(evt);
            }
        });

        finishTime.setText("00:00:00");

        playlist.setBackground(new java.awt.Color(204, 204, 204));
        playlist.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        playlist.setEditingColumn(0);
        playlist.setEditingRow(0);
        playlist.setEnabled(false);
        playlist.setGridColor(new java.awt.Color(255, 255, 255));
        playlist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        playlist.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playlistMouseClicked(evt);
            }
        });
        jScrollPane.setViewportView(playlist);

        albumCover.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/song.png"))); // NOI18N
        albumCover.setPreferredSize(new java.awt.Dimension(40, 40));

        javax.swing.GroupLayout playerPanelLayout = new javax.swing.GroupLayout(playerPanel);
        playerPanel.setLayout(playerPanelLayout);
        playerPanelLayout.setHorizontalGroup(
            playerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(playerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(playerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 353, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(playerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addGroup(playerPanelLayout.createSequentialGroup()
                            .addComponent(previous, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(play, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(next, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(35, 35, 35)
                            .addComponent(repeatButton))
                        .addGroup(playerPanelLayout.createSequentialGroup()
                            .addComponent(albumCover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(14, 14, 14)
                            .addGroup(playerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(nowPlaying)
                                .addGroup(playerPanelLayout.createSequentialGroup()
                                    .addComponent(start)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(sliderTime, javax.swing.GroupLayout.PREFERRED_SIZE, 187, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(finishTime))))))
                .addGap(0, 9, Short.MAX_VALUE))
        );
        playerPanelLayout.setVerticalGroup(
            playerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(playerPanelLayout.createSequentialGroup()
                .addGroup(playerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(playerPanelLayout.createSequentialGroup()
                        .addComponent(nowPlaying)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(playerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(finishTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sliderTime, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addComponent(start, javax.swing.GroupLayout.Alignment.TRAILING)))
                    .addComponent(albumCover, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(playerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(repeatButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(playerPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(next, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(play, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(previous, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 304, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(36, 36, 36))
        );

        menuBar.setBackground(new java.awt.Color(255, 255, 255));
        menuBar.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        menuBar.setForeground(new java.awt.Color(255, 255, 255));
        menuBar.setToolTipText("");

        file.setText("File");

        openMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenu.setText("Open");
        openMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuActionPerformed(evt);
            }
        });
        file.add(openMenu);
        file.add(jSeparator1);

        closeMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        closeMenu.setText("Close");
        closeMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeMenuActionPerformed(evt);
            }
        });
        file.add(closeMenu);

        menuBar.add(file);

        controls.setText("Controls");

        shuffleMenuButton.setText("Shuffle");
        shuffleMenuButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shuffleMenuButtonActionPerformed(evt);
            }
        });
        controls.add(shuffleMenuButton);

        repeatMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        repeatMenu.setText("Repeat");
        controls.add(repeatMenu);

        resueMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0));
        resueMenu.setText("Resume");
        resueMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resueMenuActionPerformed(evt);
            }
        });
        controls.add(resueMenu);

        pauseMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, 0));
        pauseMenu.setText("Pause");
        pauseMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseMenuActionPerformed(evt);
            }
        });
        controls.add(pauseMenu);

        stopMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, 0));
        stopMenu.setText("Stop");
        stopMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopMenuActionPerformed(evt);
            }
        });
        controls.add(stopMenu);

        skipMenu.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.InputEvent.SHIFT_MASK));
        skipMenu.setText("Skip");
        skipMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                skipMenuActionPerformed(evt);
            }
        });
        controls.add(skipMenu);

        menuBar.add(controls);

        settings.setText("Setting");

        LookandFeelButton1.setText("Classic ");
        LookandFeelButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LookandFeelButton1ActionPerformed(evt);
            }
        });
        settings.add(LookandFeelButton1);

        LookandFeelButton2.setText("Modern");
        LookandFeelButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LookandFeelButton2ActionPerformed(evt);
            }
        });
        settings.add(LookandFeelButton2);
        settings.add(jSeparator2);

        changePlayMenu.setText("Preferences");
        changePlayMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePlayMenuActionPerformed(evt);
            }
        });
        settings.add(changePlayMenu);

        LookandFeelButton3.setText("Clear Library");
        LookandFeelButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LookandFeelButton3ActionPerformed(evt);
            }
        });
        settings.add(LookandFeelButton3);

        menuBar.add(settings);

        help.setText("Help");

        patchNotes.setText("Patch Notes");
        patchNotes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                patchNotesActionPerformed(evt);
            }
        });
        help.add(patchNotes);

        readme.setText("readme");
        readme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                readmeActionPerformed(evt);
            }
        });
        help.add(readme);

        menuBar.add(help);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(playerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(playerPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void playActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playActionPerformed
        if (isPlaying == true && isPause == true) {
            resumePlaying();
        } else {
            pausePlaying();
        }
    }//GEN-LAST:event_playActionPerformed

    private void repeatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_repeatButtonActionPerformed

    }//GEN-LAST:event_repeatButtonActionPerformed

    private void previousActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousActionPerformed
        previousSong();
    }//GEN-LAST:event_previousActionPerformed

    private void nextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextActionPerformed
        nextSong();
    }//GEN-LAST:event_nextActionPerformed

    private void openMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuActionPerformed
        openFile();
    }//GEN-LAST:event_openMenuActionPerformed

    private void closeMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeMenuActionPerformed
        System.exit(0);
    }//GEN-LAST:event_closeMenuActionPerformed

    private void playlistMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playlistMouseClicked
        int index;
        if (evt.getClickCount() == 2 && !evt.isConsumed() && evt.getButton() == MouseEvent.BUTTON1) {
            evt.consume();
            index = playlist.rowAtPoint(evt.getPoint());
            listSelection(index);
        }
        if (evt.getButton() == MouseEvent.BUTTON3) {
            index = playlist.rowAtPoint(evt.getPoint());
            SongProperties newSong = new SongProperties(index);
            newSong.setAlwaysOnTop(true);
            newSong.setVisible(true);
        }
    }//GEN-LAST:event_playlistMouseClicked

    private void readmeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readmeActionPerformed
        String file = "C:\\Users\\Andrew\\Google Drive\\Programs\\MusicPlayer\\src\\library\\readme.txt";
        Runtime runtime = Runtime.getRuntime();
        try {
            Process p = runtime.exec("notepad " + file);
        } catch (IOException ex) {
            Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_readmeActionPerformed

    private void pauseMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseMenuActionPerformed
        pausePlaying();
    }//GEN-LAST:event_pauseMenuActionPerformed

    private void stopMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopMenuActionPerformed
        stopPlaying();
    }//GEN-LAST:event_stopMenuActionPerformed

    private void skipMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_skipMenuActionPerformed
        nextSong();
    }//GEN-LAST:event_skipMenuActionPerformed

    private void resueMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resueMenuActionPerformed
        resumePlaying();
    }//GEN-LAST:event_resueMenuActionPerformed

    private void LookandFeelButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LookandFeelButton1ActionPerformed
        setClassicLook();
    }//GEN-LAST:event_LookandFeelButton1ActionPerformed

    private void LookandFeelButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LookandFeelButton2ActionPerformed
        setModernLook();
    }//GEN-LAST:event_LookandFeelButton2ActionPerformed

    private void changePlayMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePlayMenuActionPerformed
        SettingsForm settings = new SettingsForm();
        settings.setVisible(true);
    }//GEN-LAST:event_changePlayMenuActionPerformed

    private void shuffleMenuButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shuffleMenuButtonActionPerformed
        if (shuffleMenuButton.isSelected()) {
            shuffle();
            playBack();
        }
    }//GEN-LAST:event_shuffleMenuButtonActionPerformed

    private void LookandFeelButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LookandFeelButton3ActionPerformed
        mLibrary.clearLibrary();
    }//GEN-LAST:event_LookandFeelButton3ActionPerformed

    private void patchNotesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_patchNotesActionPerformed
        String file = "C:\\Users\\Andrew\\Google Drive\\Programs\\MusicPlayer\\src\\library\\patch notes.txt";
        Runtime runtime = Runtime.getRuntime();
        try {
            Process p = runtime.exec("notepad " + file);
        } catch (IOException ex) {
            Logger.getLogger(MusicPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_patchNotesActionPerformed

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MusicPlayer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        java.awt.EventQueue.invokeLater(() -> {
            new MusicPlayer().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem LookandFeelButton1;
    private javax.swing.JMenuItem LookandFeelButton2;
    private javax.swing.JMenuItem LookandFeelButton3;
    private javax.swing.JLabel albumCover;
    private javax.swing.JMenuItem changePlayMenu;
    private javax.swing.JMenuItem closeMenu;
    private javax.swing.JMenu controls;
    private javax.swing.JMenu file;
    private javax.swing.JLabel finishTime;
    private javax.swing.JMenu help;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton next;
    private javax.swing.JLabel nowPlaying;
    private javax.swing.JMenuItem openMenu;
    private javax.swing.JMenuItem patchNotes;
    private javax.swing.JMenuItem pauseMenu;
    private javax.swing.JButton play;
    private javax.swing.JPanel playerPanel;
    private javax.swing.JTable playlist;
    private javax.swing.JButton previous;
    private javax.swing.JMenuItem readme;
    private javax.swing.JRadioButton repeatButton;
    private javax.swing.JMenuItem repeatMenu;
    private javax.swing.JMenuItem resueMenu;
    private javax.swing.JMenu settings;
    private javax.swing.JRadioButtonMenuItem shuffleMenuButton;
    private javax.swing.JMenuItem skipMenu;
    public javax.swing.JSlider sliderTime;
    public javax.swing.JLabel start;
    private javax.swing.JMenuItem stopMenu;
    // End of variables declaration//GEN-END:variables
}
