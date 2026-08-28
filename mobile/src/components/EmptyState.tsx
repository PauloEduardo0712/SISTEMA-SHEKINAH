import { StyleSheet, Text, View } from "react-native";

import { colors, spacing, typography } from "../theme";

type Props = {
  title: string;
  text: string;
};

export function EmptyState({ title, text }: Props) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.text}>{text}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    paddingVertical: spacing.xl,
    alignItems: "center",
    gap: spacing.xs,
  },
  title: {
    color: colors.text,
    fontSize: typography.subtitle,
    fontWeight: "800",
    textAlign: "center",
  },
  text: {
    color: colors.muted,
    fontSize: typography.body,
    textAlign: "center",
  },
});
