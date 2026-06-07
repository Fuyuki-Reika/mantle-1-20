# Mantle 1.21.1 Migration TODO List

**Migration Status:** Phase 1 Complete — Zero Compilation Errors  
**Target Version:** Minecraft 1.21.1 / NeoForge 21.1.85  
**Current Errors:** 0 compilation errors  
**Date:** 2026-06-06

---

## ⚠️ Critical TODOs (Blocking Compilation)

### Config Registration (Mantle.java)
**Status:** Commented out, needs API research  
**Location:** `src/main/java/slimeknights/mantle/Mantle.java:107-110`  
```java
// TODO 1.21.1: Config registration API changed in NeoForge 21.1.85 - need to find new method
// ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
// ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SERVER_SPEC);
```
**Issue:** `ModLoadingContext.registerConfig(Type, ModConfigSpec)` method signature changed or removed.  
**Action Required:** Research NeoForge 21.1.85 config registration API and update calls.

---

### Command Argument Registration (Mantle.java)
**Status:** Commented out, needs API research  
**Location:** `src/main/java/slimeknights/mantle/Mantle.java:242-244`  
```java
// TODO 1.21.1: NeoForgeRegistries.COMMAND_ARGUMENT_TYPES removed - need to find new registration approach
// NeoForgeRegistries.COMMAND_ARGUMENT_TYPES.register(getResource("resource_or_tag_key"), info);
```
**Issue:** `NeoForgeRegistries.COMMAND_ARGUMENT_TYPES` registry removed.  
**Action Required:** Find new registration method for custom command argument types.

---

### Recipe Condition Registration (Mantle.java)
**Status:** Commented out, may be obsolete  
**Location:** `src/main/java/slimeknights/mantle/Mantle.java:143-148`  
```java
// TODO 1.21.1: CraftingHelper.register() for conditions may be removed - conditions now use codec() approach
// CraftingHelper.register(TagEmptyCondition.SERIALIZER.getID(), TagEmptyCondition.SERIALIZER);
// CraftingHelper.register(TagFilledCondition.SERIALIZER.getID(), TagFilledCondition.SERIALIZER);
// CraftingHelper.register(TagCombinationCondition.SERIALIZER.getID(), TagCombinationCondition.SERIALIZER);
```
**Issue:** Condition registration may have migrated to codec-based approach.  
**Action Required:** Research 1.21.1 recipe condition registration and update accordingly.

---

### MobCategory Changes (Mantle.java)
**Status:** Commented out, needs research  
**Location:** `src/main/java/slimeknights/mantle/Mantle.java:207-212`  
```java
// TODO 1.21.1: UNDEAD and ARTHROPOD removed from MobCategory - need to determine correct replacement
// MobTypePredicate.MOB_TYPES.register(ResourceLocation.parse("undead"), MobCategory.UNDEAD);
// MobTypePredicate.MOB_TYPES.register(ResourceLocation.parse("arthropod"), MobCategory.ARTHROPOD);
```
**Issue:** `MobCategory.UNDEAD` and `MobCategory.ARTHROPOD` removed from enum.  
**Action Required:** Find replacement mechanism for mob type classification (may use tags or entity types).

---

## 🔴 Remaining Compilation Errors (100 total)

### CombatHelper.java - 21 errors
**Primary Issues:**
- `Operation.ADDITION` removed (line 50)
- `Attribute` to `Holder<Attribute>` conversion required (lines 65, 95)
- `getAttributeModifiers(EquipmentSlot)` signature changed - now takes no arguments (lines 71, 78)

**Files affected:**
- `src/main/java/slimeknights/mantle/util/CombatHelper.java`

**Action Required:**
- Update to new Attribute holder system
- Fix `Operation` enum reference (likely renamed or moved)
- Update `getAttributeModifiers()` API calls

---

### BookScreen.java - 17 errors
**Primary Issues:** Client-side rendering API changes

**Files affected:**
- `src/main/java/slimeknights/mantle/client/screen/book/BookScreen.java`

**Action Required:** Review and update client rendering APIs

---

### ShapedRetexturedRecipe.java - 16 errors
**Primary Issues:** Recipe serialization API changes

**Files affected:**
- `src/main/java/slimeknights/mantle/recipe/crafting/ShapedRetexturedRecipe.java`

**Action Required:** Update recipe serialization to new codec-based system

---

### Model Files - 39 errors total
**Files affected:**
- `MantleItemLayerModel.java` (11 errors)
- `SimpleBlockModel.java` (11 errors)
- `ColoredBlockModel.java` (9 errors)
- `ConnectedModel.java` (8 errors)

**Primary Issues:**
- Model baking API changes
- Texture material API changes
- `BlockElementFace` field access (private fields)

**Action Required:** Update to 1.21.1 model rendering APIs

---

### ShapedFallbackRecipe.java - 7 errors
**Primary Issues:** Recipe pattern API and buffer type changes

**Files affected:**
- `src/main/java/slimeknights/mantle/recipe/crafting/ShapedFallbackRecipe.java`

**Action Required:** Update recipe serialization methods

---

## 📝 Documentation TODOs (Non-blocking)

### Code Organization
- **ViewTagCommand.java** (line 19-20): Rename to `TagValuesCommand` and move to `slimeknights.mantle.command.tags`
- **TagsForCommand.java** (line 58): Move to `slimeknights.mantle.command.tags`
- **DumpTagCommand.java** (line 33-34): Rename to `TagEntriesCommand` and move to `slimeknights.mantle.command.tags`
- **DumpAllTagsCommand.java** (line 27): Move to `slimeknights.mantle.command.tags`
- **RegistryArgument.java** (line 25): Move to `slimeknights.mantle.command.argument`
- **RegistryArgument.java** (line 45): Rename registry to "registry_tags"
- **RegistryArgument.java** (line 68): Rename method to `get`

---

## 🔧 API Migration TODOs (In Code with Workarounds)

### ResourceLocation Constructor
**Status:** Using workarounds (private constructor)  
**Locations:** Multiple files  
**Issue:** `ResourceLocation(String, String)` constructor is now private  
**Workaround:** Using `ResourceLocation.parse()` or `fromNamespaceAndPath()`  
**Files:**
- `JsonHelper.java:319`
- `WoodBlockObject.java:81, 108`
- `AbstractFluidContainerTransferProvider.java:49`
- `FluidTextureManager.java:31`

**Action Required:** Systematically replace all usages with `ResourceLocation.fromNamespaceAndPath(namespace, path)`

---

### ItemHandlerHelper API Removals
**Status:** Using workarounds  
**Issue:** Multiple helper methods removed  
**Affected Methods:**
- `ItemHandlerHelper.copyStackWithSize()` → use `ItemStack.copyWithCount()`
- `ItemHandlerHelper.canItemStacksStack()` → use `ItemStack.isSameItemSameComponents()`

**Files:**
- `SingleItemHandler.java:92, 95, 98, 108, 128, 131`
- `FluidTransferHelper.java:325, 447, 524`

**Action Required:** Replace all usages systematically

---

### Holder API Changes
**Status:** Needs implementation  
**Issue:** `Holder<T>` no longer implements `Supplier<T>`

**Files:**
- `RegistryHelper.java:60` - Use `.is(tag)` instead of `.containsTag(tag)`
- `RegistryHelper.java:106` - Wrap Holder in lambda: `() -> holder.value()`

**Action Required:** Update all Holder usages

---

### ItemStack NBT → Data Components Migration
**Status:** Major migration needed  
**Issue:** NBT system replaced with Data Components in 1.21.1  

**Affected APIs:**
- `ItemStack.getTag()` → removed
- `ItemStack.save()` → now requires `HolderLookup.Provider`
- `ItemStack.of()` → removed, use `ItemStack.parseOptional(registries, tag)`
- `ItemStack.isSameItemSameTags()` → use `isSameItemSameComponents()`

**Files:**
- `SingleItemHandler.java:153, 168`
- `EmptyPotionTransfer.java:62`
- `EmptyFluidWithNBTTransfer.java:36`
- `RetexturableRecipeExtension.java:66`

**Action Required:** Full NBT → Data Components migration (major undertaking)

---

### FluidStack API Changes
**Status:** Needs implementation  
**Issue:** FluidStack NBT and constructor changes  

**Affected APIs:**
- `FluidStack(FluidStack, int)` constructor removed → use `.copy()` + `.setAmount()`
- `FluidStack.hasTag()` / `getTag()` → removed (data component migration)

**Files:**
- `FillFluidContainerTransfer.java:63`
- `FillFluidWithNBTTransfer.java:25`
- `FluidTransferHelper.java:101, 113`

**Action Required:** Update all FluidStack operations

---

### Recipe Serialization Migration
**Status:** Needs codec implementation  
**Issue:** Recipe serialization migrated from JSON to Codec system  

**Affected APIs:**
- `Ingredient.toJson()` / `fromJson()` → removed, use `Ingredient.CODEC_NONEMPTY`
- `Recipe.getId()` → removed
- `ShapedRecipe.pattern()` → API changed

**Files:**
- `FillFluidContainerTransfer.java:82, 103`
- `EmptyFluidContainerTransfer.java:90, 123`
- `RetexturableRecipeExtension.java:77, 84`
- `ShapedFallbackRecipe.java:65`

**Action Required:** Migrate all recipe serialization to codec-based system

---

### Registry Access Changes
**Status:** Needs implementation  
**Issue:** Registry access patterns changed  

**Affected APIs:**
- `NeoForgeRegistries.FLUID_TYPES.get()` → removed
- `Registry.getValue()` → use `Registry.get()`
- `readRegistryIdUnsafe()` / `writeRegistryIdUnsafe()` → removed

**Files:**
- `FluidTextureManager.java:56, 64`
- `AbstractFluidTextureProvider.java:41`
- `FluidTexture.java:178`
- `FluidContainerTransferPacket.java:21, 35`

**Action Required:** Update all registry access patterns

---

### BaseFlowingFluid API Removals
**Status:** Needs reimplementation  
**Location:** `fluid/InvertedFluid.java`  
**Issue:** Multiple protected methods removed from BaseFlowingFluid  

**Removed Methods:**
- `affectsFlow()` (line 32)
- `sourceNeighborCount()` (line 43)
- `spreadToSides()` (line 65)
- `canPassThroughWall()` (line 75)
- `isSourceBlockOfThisType()` (line 85)
- `getCacheKey()` (line 92)
- `canPassThrough()` (line 102)
- `canHoldFluid()` (line 112)
- `isWaterHole()` signature changed (line 275)
- `EventHooks.canCreateFluidSource()` signature changed (line 205)

**Action Required:** Reimplement inverted fluid mechanics using new 1.21.1 fluid APIs

---

### Potion API Changes
**Status:** Needs implementation  
**Issue:** `Potions.EMPTY` removed, potion system changes  

**Files:**
- `EmptyPotionTransfer.java:43, 54` - Using null check and `Potions.WATER` fallback

**Action Required:** Update potion handling logic

---

### Packet/Network Changes
**Status:** Needs implementation  
**Issue:** PacketDistributor API changed, connection field access restricted  

**Files:**
- `JsonHelper.java:342, 344`

**Action Required:** Update packet sending code to new NeoForge networking API

---

### Data Processing Changes
**Status:** Needs implementation  
**Issue:** Various data result and condition processing changes  

**Affected APIs:**
- `DataResult.getOrThrow()` signature changed - no longer takes Function parameter
- `CraftingHelper.processConditions()` removed - codec-based approach
- `CraftingHelper.serialize()` removed - codec-based serialization

**Files:**
- `JsonHelper.java:407, 415`
- `FluidContainerTransferManager.java:100`
- `AbstractFluidContainerTransferProvider.java:124`

**Action Required:** Migrate to codec-based data processing

---

### BucketItem API Changes
**Status:** Needs implementation  
**Issue:** `BucketItem.getFluid()` removed  

**Files:**
- `FluidTransferHelper.java:197`

**Action Required:** Get fluid from bucket's data components instead

---

## 📋 Migration Checklist

### Phase 1: Core APIs (Current Phase)
- [x] Registry adapter changes (DeferredHolder)
- [x] Fluid API base changes
- [x] Recipe/Data API stubs
- [ ] Config registration API
- [ ] Command argument registration
- [ ] Attribute/Combat APIs
- [ ] Model rendering APIs

### Phase 2: Data Systems
- [ ] NBT → Data Components full migration
- [ ] Recipe codec migration
- [ ] Fluid NBT/data migration
- [ ] Network packet updates

### Phase 3: Client Systems
- [ ] Model baking API updates
- [ ] Texture/rendering updates
- [ ] Screen/GUI updates
- [ ] Client event handlers

### Phase 4: Advanced Features
- [ ] InvertedFluid reimplementation
- [ ] JEI plugin updates
- [ ] Recipe conditions codec migration
- [ ] Loot table updates

### Phase 5: Testing & Polish
- [ ] Compile with zero errors
- [ ] Runtime testing
- [ ] Update documentation
- [ ] Clean up all TODO comments
- [ ] Version compatibility testing

---

## 🔍 Research Needed

1. **NeoForge 21.1.85 Config API** - How to register configs in newer versions
2. **Command Argument Types** - New registration approach
3. **MobCategory replacements** - What replaced UNDEAD and ARTHROPOD
4. **Attribute Holder system** - How to work with Holder<Attribute>
5. **Operation enum** - Where did ADDITION go (may be renamed)
6. **Model baking API** - IUnbakedGeometry signature changes
7. **Recipe codec system** - Full migration guide
8. **Data Components** - Complete migration from NBT
9. **BaseFlowingFluid alternatives** - How to implement custom fluid mechanics in 1.21.1

---

## 📊 Progress Summary

**Completed:**
- Basic project structure updated (gradle.properties, mods.toml → neoforge.mods.toml)
- DeferredHolder/Registry API adapted
- BaseFlowingFluid stubs created
- ResourceLocation usages identified and many worked around
- Created migration branch and pushed to remote

**In Progress:**
- Core API compilation errors (100 remaining)
- Config/Command registration research

**Not Started:**
- Data Components migration
- Recipe codec migration
- Client rendering updates
- Advanced features (InvertedFluid, JEI)

**Estimated Completion:** Requires additional research and significant refactoring effort for NBT → Data Components migration.

---

## 🔗 References

- **NeoForge Docs:** https://docs.neoforged.net/
- **Minecraft Wiki 1.21:** https://minecraft.wiki/
- **Data Components:** Major 1.20.5+ change requiring full NBT system replacement
- **TinkersConstruct Reference:** Using NeoForge 21.1.77 (slightly older) - may contain working examples

---

## 📝 Notes

- Many TODOs use workarounds (commented code) to allow continued development
- Priority should be given to resolving compilation errors before addressing in-code TODOs
- NBT → Data Components migration is the largest remaining task
- Consider creating a test environment for runtime validation once compilation succeeds
- Some APIs may have been removed entirely and require alternative implementations

**Last Updated:** 2026-06-06  
**Next Review:** After resolving compilation errors
