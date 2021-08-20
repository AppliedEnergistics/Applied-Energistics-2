package appeng.util;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;

public class UndoStack extends SnapshotParticipant<Integer> {
    public static void push(TransactionContext transaction, Runnable undoAction) {
        INSTANCE.updateSnapshots(transaction);
        INSTANCE.undoActions.add(undoAction);
    }

    private static final UndoStack INSTANCE = new UndoStack();
    private final List<Runnable> undoActions = new ArrayList<>();

    private UndoStack() {
    }

    @Override
    protected Integer createSnapshot() {
        return undoActions.size();
    }

    @Override
    protected void readSnapshot(Integer snapshot) {
        undoActions.subList(snapshot, undoActions.size()).clear();
    }

    @Override
    protected void onFinalCommit() {
        undoActions.clear();
    }
}
