package slimeknights.mantle.loot;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.data.GenericDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Data provider for adding new loot table injections */
public abstract class AbstractLootTableInjectionProvider extends GenericDataProvider {
  private final List<Builder> builders = new ArrayList<>();
  private final CompletableFuture<HolderLookup.Provider> lookupProvider;
  private final String domain;

  protected AbstractLootTableInjectionProvider(PackOutput output,
      CompletableFuture<HolderLookup.Provider> lookupProvider, String domain) {
    super(output, Target.DATA_PACK, LootTableInjector.FOLDER);
    this.lookupProvider = lookupProvider;
    this.domain = domain;
  }

  /** Method to add all relevant tables */
  protected abstract void addTables();

  @Override
  public final CompletableFuture<?> run(CachedOutput output) {
    addTables();
    return lookupProvider.thenCompose(provider -> allOf(builders.stream().map(builder -> {
      JsonObject json = LootTableInjection.LOADABLE.serialize(builder.build()).getAsJsonObject();
      if (!builder.conditions.isEmpty()) {
        ICondition.writeConditions(provider, json, builder.conditions);
      }
      return saveJson(output, ResourceLocation.fromNamespaceAndPath(domain, builder.path), json);
    })));
  }

  /** Creates a new injection */
  protected LootTableInjection.Builder inject(String path, ResourceLocation name, ICondition... conditions) {
    LootTableInjection.Builder builder = new LootTableInjection.Builder();
    builders.add(new Builder(path, name, builder, List.of(conditions)));
    return builder;
  }

  /** Creates a new injection for the Minecraft domain */
  // TODO 1.21.1: ResourceLocation constructor changed - use parse for potentially
  // namespaced strings
  protected LootTableInjection.Builder inject(String path, String name, ICondition... conditions) {
    return inject(path, ResourceLocation.parse(name), conditions);
  }

  /** Creates a new injection for the Minecraft domain */
  // TODO 1.21.1: ResourceLocation constructor changed - use fromNamespaceAndPath
  protected LootTableInjection.Builder injectChest(String name, ICondition... conditions) {
    return inject(name, ResourceLocation.fromNamespaceAndPath("minecraft", "chests/" + name), conditions);
  }

  /** Creates a new injection for the Minecraft domain */
  // TODO 1.21.1: ResourceLocation constructor changed - use fromNamespaceAndPath
  protected LootTableInjection.Builder injectGameplay(String name, ICondition... conditions) {
    return inject(name, ResourceLocation.fromNamespaceAndPath("minecraft", "gameplay/" + name), conditions);
  }

  /** Internal builder tuple */
  private record Builder(String path, ResourceLocation name, LootTableInjection.Builder builder,
      List<ICondition> conditions) {
    public LootTableInjection build() {
      return builder.build(name);
    }
  }
}
