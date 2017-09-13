package Les2;

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;


public class BeatBox {
    JPanel mainPanel;
    //Храним флажки в массиве ArrayList
    ArrayList<JCheckBox> checkBoxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;

    //Названия инструментов в виде строкового массива
    String [] instrumentNames = {"Bass Drum", "Closed Hi-Hat",
            "Open Hi-Hat","Acoustic Snare", "Crash Cymbal", "Hand Clap",
            "High Tom", "Hi Bongo", "Maracas", "Whistle", "Low Conga",
            "Cowbell", "Vibraslap", "Low-mid Tom", "High Agogo",
            "Open Hi Conga"};

    //числа представляющие фактические барабанные клавиши
    int [] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

    public static void main(String[] args) {
        new BeatBox().buildGUI();
    }

    public void buildGUI() {
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();
        JPanel background = new JPanel(layout);
        //пустая граница позволяет создать поля между краями панели и местом размещения компонентов
        background.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkBoxList = new ArrayList<JCheckBox>();
        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton start = new JButton("Start");
        start.addActionListener(new MyStartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new MyStopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("Tempo Up");
        upTempo.addActionListener(new MyUpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("Tempo Down");
        downTempo.addActionListener(new MyDownTempoListener());
        buttonBox.add(downTempo);

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for (int i = 0; i < 16; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);

        theFrame.getContentPane().add(background);

        GridLayout grid = new GridLayout(16, 16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        //Создаем флажки, приравниваем их к false, чтобы не были установлены, а затем добавдяем их в массив ArrayList и на панель
        for (int i = 0; i < 256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkBoxList.add(c);
            mainPanel.add(c);
        }

        setUpMidi();

        theFrame.setBounds(50, 50, 300, 300);
        theFrame.pack();
        theFrame.setVisible(true);
    }

    //Получаем синтезаторв, секвенсор, дорожку
    public void setUpMidi (){
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ, 4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Преобразуем состояния флажков в MIDI-события и добавляем их на дорожку
    public void buildTrackAndStart(){
        //Создаем массив из 16 элементов, чтобы хранить значения для каждого инструмента, на все 16 тактов
        int[] trackList = null;

        //Избавляемся от старой дородки и создаем новую
        sequence.deleteTrack(track);
        track = sequence.createTrack();

        //Делаем это для каждого из 16 рядов
        for (int i=0; i<16; i++){
            trackList = new int[16];

        //Задаем клавишу, которая представляет инструмент. Массив содержит MIDI-числа для каждого инструмента
        int key = instruments[i];

        //Делаем для каждого текушего ряда
        for (int j=0; j<16; j++){
            //Установлен ли флажок на этом такте? Если да, то помещаем значение клавиши в текущую ячейку массива (ячейку,
            //которая представляет такт). Если нет, то инструмент не должен играть в этом такте, поэтому приравнием к 0
            JCheckBox jc = (JCheckBox) checkBoxList.get(j+(16*i));
            if (jc.isSelected()){
                trackList[j]=key;
            } else {
                trackList[j]=0;
            }
        }
        //Для этого инструмента, для всех 16 тактов создаем события и добавляем их на дорожку
        makeTracks(trackList);
        track.add(makeEvent(176, 1, 27, 0, 16));
        }

        //Мы всегда должны быть уверены, что событие на такте 16 существует (они идут от 0 до 15)
        //Иначе BeatBox может не пройти все 16 тактов, перед тем как заново начнет последовательность
        track.add(makeEvent(192, 9, 1, 0, 15));
        try{
            sequencer.setSequence(sequence);
            //Позволяет задать количество повторений цикла или, как в этом случае, непрерывный цикл
            sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);
            sequencer.start();
            sequencer.setTempoInBPM(120);
        //Теперь мы проигрываем мелодию
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Внутренние классы - слушатели для кнопок
    public class MyStartListener implements  ActionListener{
        public void  actionPerformed(ActionEvent a){
            buildTrackAndStart();
        }
    }

    public class MyStopListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            sequencer.stop();
        }
    }

    //Коэффициент темпа определяет тема синтезатора
    //По умолчанию он равен 10, поэтому щелчком мыши изменить его на +/- 3%
    public class MyUpTempoListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            float temproFactor = sequencer.getTempoFactor();
                sequencer.setTempoFactor((float)(temproFactor*1.03));
        }
    }

    public class MyDownTempoListener implements ActionListener{
        public void actionPerformed(ActionEvent a){
            float tempoFactor = sequencer.getTempoFactor();
                sequencer.setTempoFactor((float)(tempoFactor*.97));
        }
    }

    //Метод создает события для одного инструмента за каждый проход цикла для всех 16 тактов
    //Можно получить int[] для Bass Drum, и каждый элемент массива будет содержать либо клавишу этого инструмента, либо 0
    //Если это 0, то инструмент не должен играт на текущем такте
    public  void  makeTracks (int[] list){
        for (int i=0; i<16; i++){
            int key = list[i];

            //Создаем события включения и выключения и добавляем их в дорожку
            if (key!=0){
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i+1));
                }
            }
        }

    public MidiEvent makeEvent (int comd, int chan, int one, int two, int tick){
        MidiEvent event = null;
        try{
            ShortMessage a = new ShortMessage();
            a.setMessage(comd,chan,one,two);
            event = new MidiEvent(a, tick);
        }catch (Exception e){
            e.printStackTrace();
        }
        return event;
    }


}
