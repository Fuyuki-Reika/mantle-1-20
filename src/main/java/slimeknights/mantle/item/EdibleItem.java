package slimeknights.mantle.item;

import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import slimeknights.mantle.util.TranslationHelper;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class EdibleItem extends Item {
  public EdibleItem(FoodProperties foodIn) {
    this(new Properties().food(foodIn));
  }

  public EdibleItem(Item.Properties properties) {
    super(properties);
    // TODO 1.21.1: foodProperties field removed - validation needs different
    // approach
    // Objects.requireNonNull(foodProperties, "Must set food to make an
    // EdibleItem");
  }

  @Override
  public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
      TooltipFlag flagIn) {
    TranslationHelper.addOptionalTooltip(stack, tooltip);
    // Show food effect descriptions in tooltip
    FoodProperties food = stack.getItem().getFoodProperties(stack, null);
    if (food != null) {
      for (FoodProperties.PossibleEffect possible : food.effects()) {
        MobEffectInstance effect = possible.effect();
        if (effect != null) {
          tooltip.add(Component.literal(I18n.get(effect.getDescriptionId()).trim()).withStyle(ChatFormatting.GRAY));
        }
      }
    }
  }
}
