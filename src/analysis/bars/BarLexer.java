package analysis.bars;

import analysis.harmonic.Tonality;
import analysis.metadata.MetadataExtractor;
import jm.music.data.Note;
import jm.music.data.Part;
import jm.music.data.Phrase;
import jm.music.data.Score;

import java.util.ArrayList;

import static java.lang.System.exit;

/**
 * Class that splits a score in an ArrayList of Bars
 */
public class BarLexer {
    private double barDuration_;
    private ArrayList<Bar> bars_;
    private double quantum_;
    private double r_;
    private double s_;
    private int barNumber_;
    private double barUnit_;
    private int beatsPerBar_;

    /**
     * Constructor. Lex the score bar by bar
     * @param score The score to lex
     */
    public BarLexer(Score score, double quantum) {
        barUnit_ = MetadataExtractor.computeBarUnit(score.getDenominator());
        beatsPerBar_ = score.getNumerator();
        barDuration_ = barUnit_ * beatsPerBar_;
        bars_ = new ArrayList<>();
        barNumber_ = (int)((score.getEndTime() + barDuration_) / barDuration_);
        quantum_ = quantum;
        r_ = 0;
        s_ = 0;
        lexBarsFromScore(score);
    }

    /**
     * Constructor that sets all parameters to null.
     */
    public BarLexer() {
        barUnit_ = 0.0;
        beatsPerBar_ = 0;
        barDuration_ = 0.0;
        bars_ = null;
        barNumber_ = 0;
        quantum_ = 0.0;
    }

    /**
     * Split the score in a list of bars
     * @param score
     */
    private void lexBarsFromScore(Score score) {
        for (int i = 0; i < barNumber_; ++i)
            bars_.add(i, new Bar());

        for (Part p : score.getPartArray()) {
            for (Phrase phrase : p.getPhraseArray()) {
                for (int i = 0; i < phrase.length(); ++i) {
                    Note note = phrase.getNote(i);
                    double time = normalizeStartTime(phrase.getNoteStartTime(i));
                    double duration = normalizeRhythm(note.getRhythmValue());
                    if (time % barDuration_ + duration > barDuration_) {
                        BarNote newHalfNote = new BarNote(0.0, (time + duration) % barDuration_, note.getPitch());
                        bars_.get((int)(time / barDuration_) + 1).addNote(newHalfNote);
                        duration = barDuration_ - (time % barDuration_);
                    }
                    BarNote newNote = new BarNote(time % barDuration_, duration, note.getPitch());
                    bars_.get((int)(time / barDuration_)).addNote(newNote);
                }
            }
        }
    }

    private double normalizeStartTime(double startTime) {
        // truncate three decimals and convert to int.
        s_ += startTime;
        int numberofbarsbefore = (int) (startTime / barDuration_);
        int result = ((int)(r_ * 10000.0));
        // Rounded to the superior quantum_ multiple.
        int mod = ((int)(quantum_ * 10000.0));
        int rest = result % mod;
        if (rest != 0.0) {
            rest = rest > mod / 2 ? mod - rest : - rest;
            result = result + rest;
        }
        s_ =  (double) - rest / 10000.0;
        return (double) result / 10000.0 + numberofbarsbefore * barDuration_;
    }

    /**
     * Correct rhythm errors in MIDI by approximating to the closest real rhythm value
     * @param rhythm
     * @return the normalized rhythm
     */
    public double normalizeRhythm(double rhythm) {
        // truncate three decimals and convert to int.
        r_ += rhythm;
        int result = ((int)(r_ * 10000.0));
        // Rounded to the superior quantum_ multiple.
        int mod = ((int)(quantum_ * 10000.0));
        int rest = result % mod;
        if (rest != 0.0) {
            rest = rest > mod / 2 ? mod - rest : - rest;
            result = result + rest;
        }
        r_ =  (double) - rest / 10000.0;
        return (double) result / 10000.0;
    }

    /**
     * Getter for the Bar list
     * @return The list of lexed bars
     */
    public ArrayList<Bar> getBars() {
        return bars_;
    }

    public int getBarNumber() {
        return barNumber_;
    }

    public double getQuantum() {
        return quantum_;
    }

    public int getBeatsPerBar() {
        return beatsPerBar_;
    }

    public double getBarUnit() {
        return  barUnit_;
    }

    @Override
    public BarLexer clone() {
        BarLexer newbl = new BarLexer();
        newbl.barDuration_ = barDuration_;
        newbl.quantum_ = quantum_;
        newbl.r_ = r_;
        newbl.barNumber_ = barNumber_;
        newbl.barUnit_ = barUnit_;
        newbl.beatsPerBar_ = beatsPerBar_;
        newbl.bars_ = new ArrayList<>();
        for (Bar b : bars_)
            newbl.bars_.add(b.clone());
        return newbl;
    }
}
