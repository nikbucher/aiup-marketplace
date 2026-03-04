package com.example.app.views;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.vaadin.addons.dramafinder.AbstractBasePlaywrightIT;
import org.vaadin.addons.dramafinder.element.button.ButtonElement;
import org.vaadin.addons.dramafinder.element.grid.GridElement;
import org.vaadin.addons.dramafinder.element.textfield.TextFieldElement;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ExampleViewIT extends AbstractBasePlaywrightIT {

    @LocalServerPort
    private int port;

    @Override
    public String getUrl() {
        return String.format("http://localhost:%d/", port);
    }

    @Override
    public String getView() {
        return "example";
    }

    @Test
    void grid_displays_data() {
        GridElement grid = GridElement.get(page);

        // Viewport may limit rendered rows — use total count
        assertThat(grid.getTotalRowCount()).isGreaterThan(0);
    }

    @Test
    void select_row_populates_form() {
        GridElement grid = GridElement.get(page);
        var cell = grid.findCell(0, "Name");
        String expectedName = cell.get().getCellContentLocator().innerText();

        grid.select(0);

        TextFieldElement nameField = TextFieldElement.getByLabel(page, "Name");
        nameField.assertValue(expectedName);
    }

    @Test
    void save_updates_grid() {
        GridElement grid = GridElement.get(page);
        grid.select(0);

        TextFieldElement nameField = TextFieldElement.getByLabel(page, "Name");
        nameField.setValue("Updated Name");

        ButtonElement saveButton = ButtonElement.getByText(page, "Save");
        saveButton.click();

        var updatedCell = grid.findCell(0, "Name");
        assertThat(updatedCell.get().getCellContentLocator()).hasText("Updated Name");
    }
}
