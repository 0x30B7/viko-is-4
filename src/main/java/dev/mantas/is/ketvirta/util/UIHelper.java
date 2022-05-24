package dev.mantas.is.ketvirta.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.function.Function;

public class UIHelper {

    private UIHelper() { }

    public static <S, T> void addTooltipToColumnCells(TableColumn<S, T> column, Function<S, String> transformer) {
        column.setCellFactory(c -> new TableCell<>() {
            final Tooltip tooltip = new Tooltip();

            {
                tooltip.setShowDelay(Duration.millis(500L));
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    tooltip.setText(transformer.apply(this.getTableRow().getItem()));
                    setText(item.toString());
                    setTooltip(tooltip);
                }
            }
        });
    }

}
