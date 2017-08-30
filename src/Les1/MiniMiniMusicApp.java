package Les1;

import javax.sound.midi.*;

public class MiniMiniMusicApp {

    public static void main(String[] args) {
        MiniMiniMusicApp mini = new MiniMiniMusicApp();
        mini.play();
    }

    public void play (){
        try {
            //Получаем синтезатор и открываем его, чтоб начать использовать (изначально закрыт)
            Sequencer player = MidiSystem.getSequencer();
            player.open();

            Sequence seq = new Sequence(Sequence.PPQ,4);

            //Запрашиваем трек последовательности
            Track track = seq.createTrack();

            //Midi события
            ShortMessage a = new ShortMessage();
            a.setMessage(144, 1, 44, 100);
            MidiEvent noteOn = new MidiEvent(a, 1);
            track.add(noteOn);

            ShortMessage b = new ShortMessage();
            b.setMessage(128, 1, 44, 100);
            MidiEvent noteOff = new MidiEvent(b, 16);
            track.add(noteOff);

            //Передаем последовательность синтезатору
            player.setSequence(seq);
            //Запускаем синтезатор
            player.start();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
