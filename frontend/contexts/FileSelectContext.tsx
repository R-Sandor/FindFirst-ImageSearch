import { Dispatch, SetStateAction, createContext, useState } from "react";

export interface FileSelectState {
  fileData: File | undefined;
  setFileData: Dispatch<SetStateAction<File | undefined>>;
}

export const FileSelectContext = createContext<FileSelectState>({
  fileData: undefined,
  setFileData: () => {},
});

export function FileSelectProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const [fileData, setFileData] = useState<File>();

  return (
    <FileSelectContext.Provider
      value={{ fileData: fileData, setFileData: setFileData }}
    >
      {children}
    </FileSelectContext.Provider>
  );
}