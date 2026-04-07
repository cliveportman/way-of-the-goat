import React from "react";
import { Text, ScrollView } from "react-native";
import * as Application from "expo-application";
import { TwContainer } from "@/core/components/TwContainer";
import { TwText } from "@/core/components/TwText";
import { Score } from "@/features/scores/components/Score";
import { FoodCat } from "@/core/enums";
import { maxScores } from "@/core/constants";
import { TwLine } from "@/core/components/TwLine";

export default function UserGuidePage() {
  return (
    <TwContainer twc="flex-1 bg-slate-950 px-3 pt-12">
      <TwContainer twc={"flex-col justify-end items-start mb-6"}>
        <TwText variant="title" twc={""}>
          User guide
        </TwText>
        <TwText variant="subtitle" twc={""}>
          How to get the most out of this app
        </TwText>
      </TwContainer>
      <TwContainer twc="flex-1 flex-col">
        <ScrollView>
          <TwText>
            This user guide is not intended to replace the inspiration behind
            it: a book called{" "}
            <Text style={{ fontStyle: "italic" }}>Racing Weight</Text> by Matt
            Fitzgerald. If you haven't already, you really should buy the book.
          </TwText>
          <TwText twc={"mb-0"}>
            That said, you're likely to have some questions, so let's try and
            preempt them here.
          </TwText>

          <TwLine />

          <TwContainer twc="flex-col">
            <TwText variant={"heading"}>How to use</TwText>
            <TwContainer twc={"flex-col justify-center mb-6"}>
              <Score
                text={"Fruit"}
                cat={FoodCat.fruit}
                servings={2}
                maxScores={maxScores.fruit}
                onPress={() => {}}
                onLongPress={() => {}}
              />
              <Score
                text={"Refined grains"}
                cat={FoodCat.refinedgrains}
                servings={3}
                maxScores={maxScores.refinedgrains}
                onPress={() => {}}
                onLongPress={() => {}}
              />
            </TwContainer>
            <TwText>
              To add a serving{" "}
              <Text style={{ fontWeight: 600 }}>tap the food category.</Text>
            </TwText>
            <TwText>
              To remove a serving{" "}
              <Text style={{ fontWeight: 600 }}>
                press and hold the food category
              </Text>
              .
            </TwText>
            <TwText twc={"mb-0"}>
              To view a different day{" "}
              <Text style={{ fontWeight: 600 }}>swipe</Text>.
            </TwText>
          </TwContainer>

          <TwLine />

          <TwText variant={"heading"}>Food categories</TwText>

          <TwText variant={"subheading"}>Vegetables</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Vegetables"}
              cat={FoodCat.veg}
              servings={6}
              maxScores={maxScores.veg}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-6">
            Raw or cooked vegetables, pulses, tomatoes, chillies, eaten whole,
            chopped, pureed, whatever. One serving might be a fist-sized portion
            of veg, a decent side salad or a bowl of soup.
          </TwText>

          <TwText variant={"subheading"}>Fruit</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Fruit"}
              cat={FoodCat.fruit}
              servings={6}
              maxScores={maxScores.fruit}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-6">
            Whole fruit, tinned fruit, canned fruit, smoothies and juices made
            with 100% fruit. One serving might be an apple or a banana, a
            handful of berries or a glass of juice. Something like apple
            crumble, you'd count as a portion of fruit and a portion of sweets.
          </TwText>

          <TwText variant={"subheading"}>Nuts + seeds + healthy oils</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Nuts + seeds"}
              cat={FoodCat.nuts}
              servings={6}
              maxScores={maxScores.nuts}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-6">
            Any nuts, seeds and healthy oils (e.g. an olive oil-based
            salad-dressing). One portion would be a handful. Nut butters without
            added sugar also count.
          </TwText>

          <TwText variant={"subheading"}>Whole grains</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Whole grains"}
              cat={FoodCat.wholegrains}
              servings={6}
              maxScores={maxScores.wholegrains}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-6">
            Whole oats, wheat and other grains, including baked goods and pastas
            made with whole grain flours. One portion would be two slices of
            bread or a bowl of porridge.
          </TwText>

          <TwText variant={"subheading"}>Dairy</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Dairy"}
              cat={FoodCat.dairy}
              servings={6}
              maxScores={maxScores.dairy}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-6">
            Unsweetened milk from cows, sheep and goats, unsweetened yoghurt,
            cheese, cream. And rocessed milks like soya milk. Small amounts of
            butter spread on bread do not count. A portion would be a glass of
            milk, two slices of cheese, a decent portion of yoghurt.
          </TwText>

          <TwText variant={"subheading"}>Lean meats + eggs</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Lean meats"}
              cat={FoodCat.leanproteins}
              servings={6}
              maxScores={maxScores.leanproteins}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-6">
            Unprocessed meats from land animals and fish. And eggs. One portion
            would be a chicken breast, regular-sized steak or fish fillet or 2
            eggs.
          </TwText>

          <TwText variant={"subheading"}>Refined grains</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Refined grains"}
              cat={FoodCat.refinedgrains}
              servings={6}
              maxScores={maxScores.refinedgrains}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-6">
            White rice, white flour, most pastas, cereals, breads and other
            baked goods. A portion would be two slices of bread, a bowl of rice
            or pasta, etc.
          </TwText>

          <TwText variant={"subheading"}>Sweets</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Sweets"}
              cat={FoodCat.sweets}
              servings={6}
              maxScores={maxScores.sweets}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-6">
            Anything with a substantial amount of sugar and anything
            artificially sweetened: sweets, pastries and other desserts, sugary
            drinks, energy bars, many breakfast cereals, yoghurts with sugar
            listed as their second ingredient.
          </TwText>

          <TwText variant={"subheading"}>Fatty (and processed) meats</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Fatty meats"}
              cat={FoodCat.fattyproteins}
              servings={6}
              maxScores={maxScores.fattyproteins}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-6">
            Meats that have been processed beyond cutting, grinding and
            seasoning: sausages, ham, bacon, corned beef, jerky, most fast
            foods.
          </TwText>

          <TwText variant={"subheading"}>Fried foods</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Fried foods"}
              cat={FoodCat.friedfoods}
              servings={6}
              maxScores={maxScores.friedfoods}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-6">
            Chips (fries), crisps, fried chicken or fish, donuts. Use your
            common sense with serving sizes.
          </TwText>

          <TwText variant={"subheading"}>Alcohol</TwText>
          <TwContainer twc={"flex-col justify-center mt-1.5 mb-1.5"}>
            <Score
              text={"Alcohol"}
              cat={FoodCat.alcohol}
              servings={6}
              maxScores={maxScores.alcohol}
              onPress={() => {}}
              onLongPress={() => {}}
            />
          </TwContainer>
          <TwText twc="mb-0">
            We're disagreeing with Matt here and penalising even one serving of
            alcohol.
          </TwText>

          <TwLine />

          <TwContainer twc="flex-col mb-0">
            <TwText variant={"heading"}>Breaking the rules</TwText>
            <TwText twc="mb-6">
              You 100% need to work out what works for you with this. Remember a
              food can cover more than one category and there are exceptions to
              every rule. Some examples:
            </TwText>
            <TwText>
              - I might dilute 250ml of milkshake with another 250ml of milk and
              call it 2 portions of Dairy and 1 portion of Sweets.
            </TwText>
          </TwContainer>

          <TwLine />

          <TwText variant={"heading"}>Other stuff</TwText>

          <TwText variant={"subheading"}>You're a vegetarian</TwText>
          <TwText twc="mb-6">
            While you could just wing it with the existing food groups, a
            vegetarian option that uses different categories is something I'd
            like to add if there is the interest. Let me know using the details
            at the bottom of the page.{" "}
          </TwText>

          <TwText variant={"subheading"}>Eating on the run</TwText>
          <TwText twc="text-white mb-6">
            Anything you eat while exercising doesn't count. So go run an
            ultramarathon and stuff your face while doing so!
          </TwText>

          <TwText variant={"subheading"}>Processed vs unprocessed</TwText>
          <TwText twc="text-white mb-6">
            Matt (author of{" "}
            <Text style={{ fontStyle: "italic" }}>Racing Weight</Text>) is quite
            keen on unprocessed food, and the science is only getting stronger.
            It's not that all processed food is bad, but you'll find most of it
            is low quality. So if you can steer clear of it, you probably
            should.
          </TwText>

          <TwText variant={"subheading"}>Protein shakes</TwText>
          <TwText twc="text-white mb-6">
            Unsweetened whey protein fits the nutrition profile of lean meats,
            so count it as that. The sweetened powders and shakes can contain
            surprising amounts of sugar or artificial sweetener though, so
            you'll need to use your judgement there - we'd probably make a large
            shake and treat is as 1 portion of lean meat and 1 portion of
            sweets.
          </TwText>

          <TwText variant={"subheading"}>
            There is already an offical app
          </TwText>
          <TwText twc="mb-6">
            There is, and I've paid for it and used it for several weeks. But
            there's lot about it that I don't like and development seems to have
            stagnated, so I built my own. If you feel bad for Matt, buy his book{" "}
            <Text style={{ fontStyle: "italic" }}>Racing Weight</Text> (or any
            of his other books). Actually, please just do that anyway!
          </TwText>

          <TwText variant={"heading"}>Technical questions</TwText>

          <TwText variant={"subheading"}>What happens to your data?</TwText>
          <TwText twc="mb-12">
            It currently stays on your device, which means that once it's gone,
            it really is gone. We are planning on adding cloud backup at some
            point.
          </TwText>

          <TwText variant={"subheading"}>
            What happens to my data if I uninstall the app?
          </TwText>
          <TwText twc="mb-12">
            It's gone for good. Reinstalling will not bring it back (the tiny
            database is destroyed).
          </TwText>

          <TwText variant={"subheading"}>Do I need an account?</TwText>
          <TwText twc="mb-12">
            No. I want people to use this app without an account and use it
            without needing an internet connection. If I add a cloud backup
            feature, that will require an account but it'll be an opt-in feature
            only.
          </TwText>

          <TwText variant={"subheading"}>Suggestions for improvement</TwText>
          <TwText twc="mb-12">
            If you have any suggestions, get in touch using{" "}
            <Text style={{ fontStyle: "italic" }}>
              wayofthegoat@theportman.co
            </Text>{" "}
            - yes, that is just a .co at the end.
          </TwText>

          <TwText variant={"small"}>{Application.applicationName}</TwText>
          <TwText variant={"small"} twc={"mb-12"}>
            {Application.nativeApplicationVersion}
          </TwText>
        </ScrollView>
      </TwContainer>
    </TwContainer>
  );
}
