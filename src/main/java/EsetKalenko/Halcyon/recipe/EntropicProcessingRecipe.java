package EsetKalenko.Halcyon.recipe;

import EsetKalenko.Halcyon.Halcyon;
import EsetKalenko.Halcyon.registry.RecipeRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class EntropicProcessingRecipe implements Recipe<RecipeInput>, HalcyonRecipe {
    private final ItemStack output;
    private final SizedIngredient input;
    private final int time;

    public EntropicProcessingRecipe(ItemStack output,
                                    SizedIngredient input, int time) {
        this.output = output;
        this.input = input;
        this.time = time;
    }
    public int getTime() {
        return time;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(input.ingredient());
        return list;
    }
    
    public NonNullList<SizedIngredient> getSizedIngredients() {
        NonNullList<SizedIngredient> list = NonNullList.create();
        list.add(input);
        return list;
    }

    @Override
    public boolean matches(RecipeInput pContainer, Level pLevel) {
        return input.test(pContainer.getItem(0));
    }

    @Override
    public ItemStack assemble(RecipeInput pContainer, HolderLookup.Provider pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider pRegistryAccess) {
        return output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return RecipeRegistry.ENTROPIC_PROCESSING_TYPE.get();
    }

    @Override
    public ResourceLocation getMachineEntry() {
        return Halcyon.locate("machinery/synthesis_chamber");
    }

    public static class Serializer implements RecipeSerializer<EntropicProcessingRecipe> {
        public static final MapCodec<EntropicProcessingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.CODEC.fieldOf("result").forGetter(r -> r.output),
                SizedIngredient.FLAT_CODEC.fieldOf("input").forGetter(r -> r.input),
                Codec.INT.fieldOf("time").forGetter((r) -> r.time)
        ).apply(instance, EntropicProcessingRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, EntropicProcessingRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, obj) -> {
                    ItemStack.STREAM_CODEC.encode(buf, obj.output);
                    SizedIngredient.STREAM_CODEC.encode(buf, obj.input);
                    buf.writeInt(obj.time);
                },
                (buf) -> {
                    ItemStack output = ItemStack.STREAM_CODEC.decode(buf);
                    SizedIngredient input = SizedIngredient.STREAM_CODEC.decode(buf);
                    int time = buf.readInt();
                    return new EntropicProcessingRecipe(output, input, time);
                }
        );

        public static final Serializer INSTANCE = new Serializer();

        @Override
        public MapCodec<EntropicProcessingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, EntropicProcessingRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}