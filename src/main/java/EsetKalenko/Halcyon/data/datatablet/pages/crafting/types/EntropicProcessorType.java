package EsetKalenko.Halcyon.data.datatablet.pages.crafting.types;

import EsetKalenko.Halcyon.recipe.EntropicProcessingRecipe;
import EsetKalenko.Halcyon.registry.BlockRegistry;
import EsetKalenko.Halcyon.registry.RecipeRegistry;
import EsetKalenko.Halcyon.screen.DataTabletScreen;
import EsetKalenko.Halcyon.data.datatablet.pages.CraftingPage;
import EsetKalenko.Halcyon.api.datatablet.CraftingType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

public class EntropicProcessorType extends CraftingType {
    @Override
    public void render(CraftingPage page, DataTabletScreen screen, GuiGraphics pGuiGraphics, int xOffset, int x, int yOffset, int y, Recipe recipe, int pMouseX, int pMouseY) {
        if (recipe instanceof EntropicProcessingRecipe recipe2) {
            pGuiGraphics.blit(DataTabletScreen.TEXTURE_CRAFTING, xOffset + x, yOffset + y, 133, 136, 123, 60);
            page.renderIngredientWithTooltip(screen, pGuiGraphics, Ingredient.of(BlockRegistry.ENTROPIC_PROCESSOR.get()), xOffset + x + 74, yOffset + y + 43, pMouseX, pMouseY);
            page.renderItemWithTooltip(pGuiGraphics, recipe.getResultItem(RegistryAccess.EMPTY), xOffset + x + 74, yOffset + y + 22, pMouseX, pMouseY);
            page.renderSizedIngredientWithTooltip(screen, pGuiGraphics, recipe2.getSizedIngredients().get(0), xOffset + x + 30, yOffset + y + 22, pMouseX, pMouseY);
            page.renderCraftTime(pGuiGraphics, recipe2.getTime(), xOffset + x + 74, yOffset + y + 8);
        }
    }

    @Override
    public boolean isRecipeType(Recipe recipe) {
        return recipe.getType().equals(RecipeRegistry.ENTROPIC_PROCESSING_TYPE.get());
    }
}
