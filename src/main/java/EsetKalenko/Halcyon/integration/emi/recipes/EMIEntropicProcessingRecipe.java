package EsetKalenko.Halcyon.integration.emi.recipes;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.integration.emi.DataNEssenceEMIPlugin;
import EsetKalenko.Halcyon.integration.emi.DataNEssenceEMIRecipe;
import EsetKalenko.Halcyon.recipe.EntropicProcessingRecipe;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class EMIEntropicProcessingRecipe extends DataNEssenceEMIRecipe {
    private final ResourceLocation id;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;
    private final int time;

    public EMIEntropicProcessingRecipe(ResourceLocation id, EntropicProcessingRecipe recipe) {
        super(DataNEssenceEMIPlugin.ENTROPIC_PROCESSING, id, null, 123, 60);
        this.id = id;
        this.input = recipe.getSizedIngredients().stream().map(s -> EmiIngredient.of(Arrays.stream(s.getItems()).map(EmiStack::of).toList())).toList();
        this.output = List.of(EmiStack.of(recipe.getResultItem(Minecraft.getInstance().level.registryAccess())));
        this.time = recipe.getTime();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return DataNEssenceEMIPlugin.ENTROPIC_PROCESSING;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return input;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public int getDisplayWidth() {
        return 123;
    }

    @Override
    public int getDisplayHeight() {
        return 60;
    }

    @Override
    public void addUnlockedWidgets(WidgetHolder widgetHolder) {
        ResourceLocation background = Halcyon.locate("textures/gui/data_tablet_crafting.png");

        widgetHolder.addTexture(background, 0, 0, getDisplayWidth(), getDisplayHeight(), 133, 136);

        // Input
        widgetHolder.addSlot(input.get(0), 29, 21).drawBack(false);
        // Output
        widgetHolder.addSlot(output.get(0), 73, 21).recipeContext(this).drawBack(false);
        widgetHolder.addText(Component.translatable("emi.halcyon.time_seconds", (double) time / 20), 82, 44, 0xffffff, false).horizontalAlign(TextWidget.Alignment.CENTER);
    }
}
