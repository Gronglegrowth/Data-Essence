package EsetKalenko.Halcyon.data.datatablet.pages;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.moddata.ClientPlayerUnlockedEntries;
import EsetKalenko.Halcyon.recipe.HalcyonRecipe;
import EsetKalenko.Halcyon.recipe.IHasRequiredKnowledge;
import EsetKalenko.Halcyon.screen.DataTabletScreen;
import EsetKalenko.Halcyon.api.datatablet.PageSerializer;
import EsetKalenko.Halcyon.api.datatablet.CraftingType;
import EsetKalenko.Halcyon.api.datatablet.CraftingTypes;
import EsetKalenko.Halcyon.data.datatablet.pages.serializers.CraftingPageSerializer;
import com.ibm.icu.lang.UCharacter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.SubStringSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CraftingPage extends TextPage {
    public List<ResourceLocation> recipes;
    public boolean showTooltip;
    public List<FormattedCharSequence> tooltipToShow = new ArrayList<>();

    public CraftingPage(Component text, boolean rtl, List<ResourceLocation> recipes) {
        super(text, rtl);
        this.recipes = recipes;
    }

    @Override
    public int textYOffset() {
        int x = 4;
        int y = 4;
        int o = 0;
        for (ResourceLocation i : recipes) {
            x += 125;
            if (o >= 2) {
                x = 4;
                y += 62;
                o = 0;
            }
            o++;
        }
        return y+58;
    }

    @Override
    public void render(DataTabletScreen screen, GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY, int xOffset, int yOffset) {
        super.render(screen, pGuiGraphics, pPartialTick, pMouseX, pMouseY, xOffset, yOffset);
        int x = 4;
        int y = 4;
        int p = 0;
        for (ResourceLocation recipeId : recipes) {
            Optional<? extends RecipeHolder<?>> holder = Minecraft.getInstance().level.getRecipeManager().byKey(recipeId);
            if (holder.isPresent()) {
                for (CraftingType craftingType : CraftingTypes.types) {
                    var recipe = holder.get().value();

                    if (craftingType.isRecipeType(recipe)) {

                        var isUnlocked = false;

                        if (recipe instanceof IHasRequiredKnowledge dataLockedRecipe)
                            isUnlocked = ClientPlayerUnlockedEntries.getUnlocked().contains( dataLockedRecipe.getEntry())
                                    || ( ClientPlayerUnlockedEntries.getIncomplete().containsKey(dataLockedRecipe.getEntry())
                                    && ClientPlayerUnlockedEntries.getIncomplete().get(dataLockedRecipe.getEntry()) >= dataLockedRecipe.getCompletionStage() );

                        if (recipe instanceof HalcyonRecipe dneRecipe)
                            isUnlocked = ClientPlayerUnlockedEntries.getUnlocked().contains( dneRecipe.getMachineEntry() );

                        if ( isUnlocked || !(recipe instanceof IHasRequiredKnowledge) ) {
                            craftingType.render(this, screen, pGuiGraphics, xOffset, x, yOffset, y, holder.get().value(), pMouseX, pMouseY);
                        } else {
                            renderLocked(pGuiGraphics, xOffset, x, yOffset, y);
                        }
                        break;
                    }
                }
            }
            x += 125;
            p++;
            if (p >= 2) {
                x = 4;
                y += 62;
                p = 0;
            }
        }
    }

    public void renderLocked(GuiGraphics graphics, int xOffset, int x, int yOffset, int y) {
        int backgroundWidth = 123;
        int backgroundHeight = 60;

        graphics.blit( Halcyon.locate("textures/gui/emi_recipe_locked.png") , xOffset + x, yOffset + y, 0, 0, backgroundWidth, backgroundHeight);

        // forgive the bad code, adapted from TextPage while terrified ~Eset
        var line1 = Component.translatable("emi.halcyon.recipe_locked_line_1");
        var line2 = Component.translatable("emi.halcyon.recipe_locked_line_2");
        int textX;
        FormattedCharSequence sequence1;
        FormattedCharSequence sequence2;

        textX = xOffset+((backgroundWidth - 4)-Minecraft.getInstance().font.width(line1));
        SubStringSource substr = SubStringSource.create(line1, UCharacter::getMirror, (str) -> str);
        sequence1 = FormattedCharSequence.composite(substr.substring(0, substr.getPlainText().length(), false));
        graphics.drawString(Minecraft.getInstance().font, sequence1, textX+x, (yOffset + y) + (backgroundHeight / 2), 0xFFFF96B5, false);

        textX = xOffset+((backgroundWidth - 4)-Minecraft.getInstance().font.width(line2));
        substr = SubStringSource.create(line2, UCharacter::getMirror, (str) -> str);
        sequence2 = FormattedCharSequence.composite(substr.substring(0, substr.getPlainText().length(), false));
        graphics.drawString(Minecraft.getInstance().font, sequence2, textX+x, (yOffset + y) + (backgroundHeight / 2 + 10), 0xFFF25EA1, false);
    }

    @Override
    public void renderPost(DataTabletScreen screen, GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY, int xOffset, int yOffset) {
        super.renderPost(screen, pGuiGraphics, pPartialTick, pMouseX, pMouseY, xOffset, yOffset);
        if (showTooltip) {
            showTooltip = false;
            pGuiGraphics.renderTooltip(Minecraft.getInstance().font, tooltipToShow, pMouseX, pMouseY);
        }
    }

    public void renderItemWithTooltip(GuiGraphics graphics, ItemStack item, int x, int y, int mouseX, int mouseY) {
        graphics.renderItem(item, x, y);
        graphics.renderItemDecorations(Minecraft.getInstance().font, item, x, y);
        if (mouseX >= x && mouseY >= y) {
            if (mouseX <= x + 16 && mouseY <= y + 16) {
                showTooltip = true;
                tooltipToShow.clear();
                for (Component i : Screen.getTooltipFromItem(Minecraft.getInstance(), item)) {
                    tooltipToShow.add(i.getVisualOrderText());
                }
            }
        }
    }

    public void renderIngredientWithTooltip(DataTabletScreen screen, GuiGraphics graphics, Ingredient item, int x, int y, int mouseX, int mouseY) {
        if (!item.isEmpty()) {
            ItemStack currentItem = item.getItems()[(screen.ticks / 20) % item.getItems().length];
            graphics.renderItem(currentItem, x, y);
            if (mouseX >= x && mouseY >= y) {
                if (mouseX <= x + 16 && mouseY <= y + 16) {
                    showTooltip = true;
                    tooltipToShow.clear();
                    for (Component i : Screen.getTooltipFromItem(Minecraft.getInstance(), currentItem)) {
                        tooltipToShow.add(i.getVisualOrderText());
                    }
                }
            }
        }
    }

    public void renderSizedIngredientWithTooltip(DataTabletScreen screen, GuiGraphics graphics, SizedIngredient item, int x, int y, int mouseX, int mouseY) {
        if (!item.ingredient().isEmpty()) {
            ItemStack currentItem = item.getItems()[(screen.ticks / 20) % item.getItems().length];
            // TODO count does not actually render???
            graphics.renderItem(currentItem, x, y);
            if (mouseX >= x && mouseY >= y) {
                if (mouseX <= x + 16 && mouseY <= y + 16) {
                    showTooltip = true;
                    tooltipToShow.clear();
                    for (Component i : Screen.getTooltipFromItem(Minecraft.getInstance(), currentItem)) {
                        tooltipToShow.add(i.getVisualOrderText());
                    }
                }
            }
        }
    }

    public void renderCraftTime(GuiGraphics graphics, int ticks, int x, int y) {
        double seconds = (double) ticks / 20;
        Component text = Component.translatable("emi.halcyon.time_seconds", seconds);
        var font = Minecraft.getInstance().font;

        graphics.drawString(font, text, (x+8) - font.width(text) / 2, y, 0xffffff, false);
    }

    public void renderFluidIngredientWithTooltip(DataTabletScreen screen, GuiGraphics graphics, FluidIngredient fluid, int x, int y, int mouseX, int mouseY) {
        if (!fluid.isEmpty()) {
            FluidStack currentFluid = fluid.getStacks()[(screen.ticks / 20) % fluid.getStacks().length];
            renderFluidWithTooltip(graphics, currentFluid, x, y, mouseX, mouseY);
        }
    }

    public void renderFluidWithTooltip(GuiGraphics graphics, FluidStack fluid, int x, int y, int mouseX, int mouseY) {
        IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluid);
        if (stillTexture != null) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
            int tintColor = fluidTypeExtensions.getTintColor(fluid);
            Color tint = new Color(tintColor);
            graphics.blit(x, y, 0, 16, 16, sprite, (float)tint.getRed()/255f, (float)tint.getGreen()/255f, (float)tint.getBlue()/255f, (float)tint.getAlpha()/255f);
        }
        if (mouseX <= x+16 && mouseY <= y+16 && mouseX >= x && mouseY >= y) {
            if (!fluid.isEmpty()) {
                showTooltip = true;
                tooltipToShow.clear();
                tooltipToShow.add(Component.translatable("gui.widget.fluid.without_max", fluid.getAmount(), fluid.getHoverName()).getVisualOrderText());
            } else {
                showTooltip = true;
                tooltipToShow.clear();
                tooltipToShow.add(Component.empty().getVisualOrderText());
            }
        }
    }

    @Override
    public PageSerializer getSerializer() {
        return CraftingPageSerializer.INSTANCE;
    }
}
