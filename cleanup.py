import os
import shutil

path = r"C:\Users\Youcode\Desktop\briefs\TraceAndTrust\src\main\java\c:"
if os.path.exists(path):
    print(f"Deleting {path}...")
    shutil.rmtree(path)
    print("Done.")
else:
    print(f"Path {path} not found.")

# Also check for any other 'c:' in DTO
path_dto = r"C:\Users\Youcode\Desktop\briefs\TraceAndTrust\src\main\java\org\usermanagement\traceandtrust\dto\c:"
if os.path.exists(path_dto):
    print(f"Deleting {path_dto}...")
    shutil.rmtree(path_dto)
    print("Done.")
