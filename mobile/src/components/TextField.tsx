import { StyleSheet, Text, TextInput, type TextInputProps, View } from "react-native";

import { colors, radius, spacing, typography } from "../theme";

type Props = TextInputProps & {
  label: string;
};

export function TextField({ label, ...props }: Props) {
  return (
    <View style={styles.container}>
      <Text style={styles.label}>{label}</Text>
      <TextInput
        placeholderTextColor={colors.muted}
        style={styles.input}
        {...props}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    gap: spacing.xs,
  },
  label: {
    color: colors.text,
    fontSize: typography.caption,
    fontWeight: "800",
    textTransform: "uppercase",
  },
  input: {
    minHeight: 48,
    borderRadius: radius.md,
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: spacing.md,
    color: colors.text,
    fontSize: typography.body,
    backgroundColor: colors.surface,
  },
});
