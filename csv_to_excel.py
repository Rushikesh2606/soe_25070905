import pandas as pd
df = pd.read_csv("test_cases_whitebox.csv")
df.to_excel("test_cases_whitebox.xlsx", index=False)
print("Excel file generated successfully!")
